package de.dheinrich.photoplus

import com.google.gdata.data.PlainTextConstruct
import com.google.gdata.client.photos.PicasawebService
import com.google.gdata.data.photos._
import java.io.FileInputStream
import java.net.URL
import java.nio.file.Files
import java.util.ResourceBundle
import javafx.scene.input.MouseEvent
import javafx.scene.{ control => jfxc }
import javafx.{ fxml => jfxf }
import javafx.{ scene => jfxs }
import javafx.scene.control.{cell => jfxscc}
import scala.collection.mutable.Buffer
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property._
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.control.cell.{ProgressBarTableCell, TextFieldListCell}
import javafx.scene.input.DragEvent
import javafx.scene.input.KeyEvent
import scalafx.scene.input.TransferMode
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.FlowPane
import scalafx.util.StringConverter
import javax.imageio.ImageIO
import scala.collection.JavaConversions._
import eu.hansolo.fx.validation.ValidationPane
import eu.hansolo.fx.validation.Validator.State._
import java.io.File
import scala.concurrent._
import ExecutionContext.Implicits.global
import darwin.util.image.ImageUtil2

object PhotoPlus extends JFXApp {
  val root: jfxs.Parent = jfxf.FXMLLoader.load(getClass.getResource("/skel.fxml"))
  stage = new PrimaryStage() {
    title = "FXML GridPane Demo"
    scene = new Scene(root)
  }
  
  val picassaUrl = "https://picasaweb.google.com/data/feed/api/user/default"
}

case class ImageProgress(url_ : String) {
  val url = StringProperty(url_)
  val progress = ObjectProperty(new java.lang.Double(-1))
}

class PhotoPlusController extends jfxf.Initializable {  
  
  //login pane
  @jfxf.FXML
  var loginPane: jfxs.layout.Pane = _
  @jfxf.FXML
  var username: jfxc.TextField = _
  @jfxf.FXML
  var password: jfxc.PasswordField = _
  @jfxf.FXML
  var loginValidation: ValidationPane = _
  
  //main app pane
  @jfxf.FXML
  var appPane: jfxs.layout.Pane = _
  @jfxf.FXML
  var albums: jfxc.ComboBox[String] = _
  @jfxf.FXML
  var button: jfxc.Button = _
  @jfxf.FXML
  var totalProgress: jfxc.ProgressBar = _
  //tabel
  @jfxf.FXML
  var imageTable: jfxc.TableView[ImageProgress] = _
  @jfxf.FXML
  var progressColumn: jfxc.TableColumn[ImageProgress, java.lang.Double] = _
  @jfxf.FXML
  var urlColumn: jfxc.TableColumn[ImageProgress, String] = _
  var images = new ObservableBuffer[ImageProgress]
  
  override def initialize(url: URL, rb: ResourceBundle) {
    ProgressBarTableCell.forTableColumn
   
    urlColumn.cellValueFactory = { _.value.url }
    progressColumn.setCellFactory(jfxscc.ProgressBarTableCell.forTableColumn())
    progressColumn.cellValueFactory = { _.value.progress }
    new TableView(imageTable).items = images
    
    loginValidation.addAll(username, password)
  }

  @jfxf.FXML
  def onFilesDropped(event: DragEvent) {
    val db = event.getDragboard();
    if (db.hasFiles()) {
      val files = db.getFiles.dropRight(1)      
      println(s"loading ${files.size} Images...")

      for (file <- files) {
        images += ImageProgress(file.getAbsolutePath)
      }
    }
    event.setDropCompleted(db.hasFiles());
    event.consume();
  }
  
  @jfxf.FXML
  def validate(ev: KeyEvent){
    println(ev.text)
    val state = if(ev.text isEmpty) INVALID else VALID    
    loginValidation.setState(ev.source.asInstanceOf[jfxs.Node], state)
  }

  @jfxf.FXML
  def checkDragContend(event: DragEvent) {
    val db = event.getDragboard();
    if (db.hasFiles()) {
      event.acceptTransferModes(TransferMode.COPY);
    } else {
      event.consume();
    }
  }

  @jfxf.FXML
  def onLogin(){
    println("loging in...")
    try{
      val a = getAlbums(username.getText, password.getText)  
      val b = new ObservableBuffer[String]
      b.appendAll(a)
      albums.items = b   
    
      loginPane.setVisible(false)    
      appPane.setDisable(false)
    }catch{
      case _:Throwable => {
          println("uppss")          
          loginValidation.setState(username, INVALID)
          loginValidation.setState(password, INVALID)
        }
    }
  }
  
  @jfxf.FXML
  def onUpload() {
//
//    val myAlbum = new AlbumEntry();
//
//    myAlbum.setTitle(new PlainTextConstruct("Trip to France"));
//    myAlbum.setDescription(new PlainTextConstruct("My recent trip to France was delightful!"));
//
//    val postUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default");
//    val insertedEntry = myService.insert(postUrl, myAlbum);
  }
  
  def getAlbums(username:String, password:String) = {
    val myService = new PicasawebService("exampleCo-exampleApp-1")
    myService.setUserCredentials(username, password)

    val feedUrl = new URL(PhotoPlus.picassaUrl + "?kind=album&access=all");

    val myUserFeed = myService.getFeed(feedUrl, classOf[UserFeed]);

    val albums:Seq[GphotoEntry[_]] = myUserFeed.getEntries().asInstanceOf[java.util.List[GphotoEntry[_]]]    
    albums map(_.getTitle.getPlainText)
  }
  
  implicit def toSfxTableColumn[E,S](c:jfxs.control.TableColumn[E,S]) = new TableColumn(c)

}
