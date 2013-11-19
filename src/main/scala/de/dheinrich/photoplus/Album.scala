package de.dheinrich.photoplus

import com.google.gdata.client.photos.PicasawebService
import com.google.gdata.data.photos.{PhotoEntry, GphotoEntry, AlbumFeed, AlbumEntry}
import com.google.gdata.data.PlainTextConstruct
import com.google.gdata.client.Query
import java.io.File
import java.nio.file.{Path, Files}
import com.google.gdata.data.media.MediaFileSource
import scala.collection.JavaConverters._

/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 17.11.13
 * Time: 00:32
 * To change this template use File | Settings | File Templates.
 */

class Album(base: GphotoEntry[_]) {

  import PicasaService._
  import ImageOps._

  val entry = new AlbumEntry(base)
  def feedUrl = serviceUrl / "albumid" / entry.getGphotoId

  def photos(implicit service: PicasawebService) = {
    val photoFeed = service.getFeed(feedUrl, classOf[AlbumFeed])
    photoFeed.entries(new PhotoEntry(_))
  }

  def upload(file: Path, title: Option[String] = None, description: Option[String] = None, client: String = "PhotoPlus")(implicit service: PicasawebService) = {
    val mime = Files.probeContentType(file)
    if (!ALLOWED_MIME.contains(mime)) sys.error(s"not allowed mime-type: $mime")
    if (file.toFile.length() > 20 * 1000 * 1000) sys.error("file bigger than 20MB")

    val myPhoto = new PhotoEntry()

    for (t <- title) myPhoto.setTitle(new PlainTextConstruct(t))
    for (d <- description) myPhoto.setDescription(new PlainTextConstruct(d))
    myPhoto.setClient(client)

    val myMedia = new MediaFileSource(file.toFile, mime)
    myPhoto.setMediaSource(myMedia)

    service.insert(feedUrl, myPhoto)
  }

  def uploadScaled(file: Path, title: Option[String] = None, description: Option[String] = None, client: String = "PhotoPlus")(implicit service: PicasawebService) = {
    val scaled = scale(file)
    try {
      upload(scaled, title, description, client)(service)
    } finally {
      scaled.toFile.delete()
    }
  }

  //def delete() = entry.delete()

  def title = entry.getTitle.getPlainText
}


object Album {

  import PicasaService._

  val MAX_ENTRIES = 1000

  def create(name: String, description: String = "")(implicit service: PicasawebService) = {
    val myAlbum = new AlbumEntry()

    myAlbum.setTitle(new PlainTextConstruct(name))
    myAlbum.setDescription(new PlainTextConstruct(description))

    new Album(service.insert(serviceUrl, myAlbum))
  }

  def all(implicit service: PicasawebService) = {
    val albumQuery = new Query(serviceUrl)
    albumQuery.setFields("entry(title,gphoto:id)")

    val partialFeed = service.query(albumQuery, classOf[AlbumFeed])

    partialFeed.entries(new Album(_))
  }

  def albumCache(implicit service: PicasawebService) = {
    all(service).groupBy(_.title).mapValues(_.head).withDefault(create(_)(service))
  }
}
