package de.dheinrich.photoplus

import java.io._
import java.nio.file.{Path, Files}
import scala.annotation.tailrec
import com.google.gdata.client.photos.PicasawebService
import scala.async.Async.{async, await}
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 18.11.13
 * Time: 19:58
 * To change this template use File | Settings | File Templates.
 */


case class Job(album: String = "", files: Seq[File] = Seq())

object App {

  sealed trait Mode

  object Nope extends Mode

  object AddJob extends Mode

  object Run extends Mode

  case class Config(mode: Mode = Nope, job: Job = Job(), user: String = null, password: String = null)

  val parser = new scopt.OptionParser[Config]("PhotoPlus") {
    head("PhotoPlus", "0.1")
    cmd("run") action ((_, c) => c.copy(mode = Run)) children {
      opt[String]('u', "username") action ((x, c) => c.copy(user = x)) required()
      opt[String]('p', "password") action ((x, c) => c.copy(password = x)) required()
    }
    cmd("addJob") action ((_, c) => c.copy(mode = AddJob)) children {
      arg[File]("<file>...") unbounded() required() action ((x, c) => c.copy(job = c.job.copy(files = c.job.files :+ x))) validate {
        f =>
          if (f.exists()) success else failure(s"File: $f doesn't exist")
      }
      opt[String]('a', "album") required() action ((x, c) => c.copy(job = c.job.copy(album = x))) validate {
        s =>
          if (s.isEmpty) failure("need a album name") else success
      }
    }
    checkConfig(c => if (c.mode != Nope) success else failure("No run mode chooses"))
  }

  def main(args: Array[String]) {
    for (config <- parser.parse(args, Config())) {
      config.mode match {
        case _: AddJob.type => Persistance.addJob(config.job)
        case _: Run.type =>
          PicasaService.login(config.user, config.password) match {
            case Right(s) => run(s)
            case _ => println("Can't login to Picasa")
          }
      }
    }
  }

  def run(service: PicasawebService) {
    implicit val s = service
    val albums = Album.albumCache

    def uploadTo(names: Stream[String], images: Seq[Path]) {
      val album = albums(names.head)
      val photos = album.photos map (_.getTitle)
      val toUpload = images filterNot (photos contains _.getFileName.toString)

      val (intoThis, next) = toUpload.splitAt(Album.MAX_ENTRIES - photos.size)

      println(s"Uploading ${intoThis.size} from ${images.size} photos to album: ${names.head}")
      for (img <- intoThis.grouped(10)) {
        println("\tuploading next 10 entries")
        val uploaded = for (i <- img) yield {
          async {
            val scaled = async(ImageOps.scale(i))
            album.upload(await(scaled), Some(i.getFileName.toString))
            await(scaled).toFile.delete()
          }
        }

        Await.ready(Future.sequence(uploaded), 1 day)
      }

      if (!next.isEmpty)
        uploadTo(names.tail, next)
    }

    for ((albumName, files) <- Persistance.jobs) {
      val images = retrieve(files.map(_.toPath))
      uploadTo(albumNameStream(albumName), images.toSeq)
    }
  }

  def albumNameStream(name: String) = name #:: Stream.from(2).map(name + '_' + _)

  @tailrec
  def retrieve(paths: Traversable[Path], found: Set[Path] = Set()): Set[Path] = {
    import scala.collection.JavaConverters._
    val (dirs, files) = paths.filterNot(Files.isSymbolicLink(_)).partition(Files.isDirectory(_))
    val images = found ++ (files filter ImageOps.isAllowedMime)

    if (dirs.isEmpty)
      images.map(_.toAbsolutePath)
    else
      retrieve(dirs.flatMap(Files.newDirectoryStream(_).asScala), images)
  }
}

