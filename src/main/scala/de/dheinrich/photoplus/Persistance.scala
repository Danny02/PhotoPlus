package de.dheinrich.photoplus

import java.io._
import scala.pickling._
import binary._

/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 18.11.13
 * Time: 23:07
 * To change this template use File | Settings | File Templates.
 */
object Persistance {

  class FilePickler(implicit val format: PickleFormat)
    extends SPickler[File] with Unpickler[File] {

    private val stringUnpickler = implicitly[Unpickler[String]]

    override def pickle(picklee: File, builder: PBuilder) = {
      builder.beginEntry(picklee)
      builder.putField("path",
        b => b.hintTag(FastTypeTag.ScalaString).beginEntry(picklee.toString).endEntry()
      )
      builder.endEntry()
    }

    override def unpickle(tag: => FastTypeTag[_], reader: PReader): File = {
      reader.hintTag(FastTypeTag.ScalaString)
      val tag = reader.beginEntry()
      val pathUnpickled = stringUnpickler.unpickle(tag, reader).asInstanceOf[String]
      reader.endEntry()

      new File(pathUnpickled)
    }
  }

  implicit val filePickler = new FilePickler


  val JOBS_FILE = new File("jobs")
  type Jobs = Map[String, Set[File]]

  def addJob(j: Job) {
    val jbs = jobs.withDefault(s => Set.empty[File])
    val merge = jbs(j.album) ++ j.files
    jobs = jbs.updated(j.album, merge)
  }

  def jobs: Jobs = {
    if (JOBS_FILE.exists()) {
      val bis = new BufferedInputStream(new FileInputStream(JOBS_FILE))
      try {
        val bArray = Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray
        bArray.unpickle[Jobs]
      } finally
        bis.close()
    } else
      Map.empty
  }

  def jobs_=(j: Jobs) {
    val bos = new BufferedOutputStream(new FileOutputStream(JOBS_FILE))
    bos.write(j.pickle.value)
    bos.close()
  }
}
