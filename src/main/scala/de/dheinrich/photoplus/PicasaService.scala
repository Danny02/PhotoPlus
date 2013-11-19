package de.dheinrich.photoplus

import scala.util.control.Exception._
import com.google.gdata.client.photos.PicasawebService
import java.net.URL
import scala.collection.JavaConverters._
import com.google.gdata.data.media.MediaFeed
import java.nio.file.{Path, Files}
import java.io.File
import com.google.gdata.data.photos.{GphotoFeed, GphotoEntry}

/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 16.11.13
 * Time: 18:49
 * To change this template use File | Settings | File Templates.
 */
object ImageOps {

  val ALLOWED_MIME = Seq("image/bmp", "image/gif", "image/jpeg", "image/png")
  val MAX_IMAGE_SIZE = 2048
  //images smaller than 2048 don't count to picasa quota
  val QUALITY = 75

  def isAllowedMime(f: Path) = {
    val mime = Files.probeContentType(f)
    ALLOWED_MIME contains mime
  }

  def scale(in: Path) = {
    import scala.sys.process._
    val abs = (_: Path).toAbsolutePath.toString
    val out = File.createTempFile("scaled_image", ".jpg").toPath
    Seq("convert", "-resize", s"${MAX_IMAGE_SIZE}x$MAX_IMAGE_SIZE", "-quality", QUALITY + "%", abs(in), abs(out)).!
    out
  }
}

object PicasaService {

  implicit class UrlOps(url: URL) {
    def /(o: String) = new URL(url, o + '/')

    def ?(p: (String, String)*) = new URL(url.toString + p.map(t => t._1 + "=" + t._2).mkString("?", "&", ""))
  }

  implicit class FeedOps(f: GphotoFeed[_]) {
    def entries[T](constr: GphotoEntry[_] => T) = f.getEntries.asScala.toSeq.map(constr(_))
  }

  val serviceUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/")

  def login(username: String, password: String) = allCatch.either {
    val myService = new PicasawebService("dheinrich-PhotoPlus-0.1")
    myService.setUserCredentials(username, password)
    myService
  }
}
