//package de.dheinrich.photoplus
//
//import com.google.gdata.client.photos.PicasawebService
//import java.net.URL
//import java.util.ResourceBundle
//import javafx.scene.{control => jfxc}
//import javafx.{fxml => jfxf}
//import javafx.{scene => jfxs}
//import javafx.scene.control.{cell => jfxscc}
//import scalafx.Includes._
//import scalafx.application.JFXApp
//import scalafx.beans.property._
//import scalafx.collections.ObservableBuffer
//import scalafx.scene.control._
//import scalafx.scene.control.cell.{ProgressBarTableCell, TextFieldListCell}
//import javafx.scene.input.DragEvent
//import javafx.scene.input.KeyEvent
//import scalafx.scene.input.TransferMode
//import scalafx.util.StringConverter
//import scala.collection.JavaConverters._
//import eu.hansolo.fx.validation.ValidationPane
//import eu.hansolo.fx.validation.Validator.State._
//import java.io.File
//
//object PhotoPlus extends JFXApp {
//  //val root: jfxs.Parent = jfxf.FXMLLoader.load(getClass.getResource("/skel.fxml"))
//  //stage = new PrimaryStage() {
//  //  title = "FXML GridPane Demo"
//  //  scene = new Scene(root)
//  //}
//}
//
//case class ImageProgress(url_ : String) {
//  val url = StringProperty(url_)
//  val progress = ObjectProperty(new java.lang.Double(-1))
//}
//
//class PhotoPlusController extends jfxf.Initializable {
//  implicit var service: PicasawebService = _
//
//  //login pane
//  @jfxf.FXML
//  var loginPane: jfxs.layout.Pane = _
//  @jfxf.FXML
//  var username: jfxc.TextField = _
//  @jfxf.FXML
//  var password: jfxc.PasswordField = _
//  @jfxf.FXML
//  var loginValidation: ValidationPane = _
//
//  //main app pane
//  @jfxf.FXML
//  var appPane: jfxs.layout.Pane = _
//  @jfxf.FXML
//  var albums: jfxc.ComboBox[Album] = _
//  @jfxf.FXML
//  var button: jfxc.Button = _
//  @jfxf.FXML
//  var totalProgress: jfxc.ProgressBar = _
//
//  //tabel
//  @jfxf.FXML
//  var imageTable: jfxc.TableView[ImageProgress] = _
//  @jfxf.FXML
//  var progressColumn: jfxc.TableColumn[ImageProgress, java.lang.Double] = _
//  @jfxf.FXML
//  var urlColumn: jfxc.TableColumn[ImageProgress, String] = _
//  var images = new ObservableBuffer[ImageProgress]
//
//  override def initialize(url: URL, rb: ResourceBundle) {
//    ProgressBarTableCell.forTableColumn
//
//    urlColumn.cellValueFactory = {
//      _.value.url
//    }
//    progressColumn.setCellFactory(jfxscc.ProgressBarTableCell.forTableColumn())
//    progressColumn.cellValueFactory = {
//      _.value.progress
//    }
//    new TableView(imageTable).items = images
//
//    loginValidation.addAll(username, password)
//
//    val fromString = (s: String) => {
//      if (!s.isEmpty) {
//        Album.create(s)(service)
//      } else null
//    }
//
//    val toString = (e: Album) => if (e != null) e.title else ""
//
//    val converter = StringConverter[Album](fromString, toString);
//    val cellFactory = TextFieldListCell.forListView(converter)
//    albums.cellFactory = cellFactory
//
//    albums.getSelectionModel.selectedItem onChange ((_, _, selected: Any) => {
//      selected match {
//        case a: Album =>
//          images.clear
//          a.photos map (_.getMediaThumbnails.get(0).getUrl) foreach addToPanel
//      }
//    })
//  }
//
//  @jfxf.FXML
//  def onFilesDropped(event: DragEvent) {
//    val db = event.getDragboard();
//    if (db.hasFiles()) {
//      val files = db.getFiles.asScala.dropRight(1)
//      println(s"loading ${files.size} Images...")
//
//      files foreach addToPanel
//    }
//    event.setDropCompleted(db.hasFiles());
//    event.consume();
//  }
//
//  def addToPanel(f: File) = {
//    images += ImageProgress(f.getAbsolutePath)
//  }
//
//  def addToPanel(u: String) = {
//    images += ImageProgress(u)
//  }
//
//  @jfxf.FXML
//  def validate(ev: KeyEvent) {
//    ev.source match {
//      case field: jfxc.TextField =>
//        val state = if (field.getCharacters.length == 0) INVALID else VALID
//        loginValidation.setState(field, state)
//    }
//  }
//
//  @jfxf.FXML
//  def checkDragContend(event: DragEvent) {
//    val db = event.getDragboard();
//    if (db.hasFiles()) {
//      event.acceptTransferModes(TransferMode.COPY);
//    } else {
//      event.consume();
//    }
//  }
//
//  @jfxf.FXML
//  def onLogin() {
//    println("loging in...")
//    PicasaService.login(username.getText, password.getText) match {
//      case Right(serv) =>
//        service = serv
//        albums.items = ObservableBuffer(Album.all(service))
//        loginPane.setVisible(false)
//        appPane.setDisable(false)
//      case _ =>
//        println("uppss")
//        loginValidation.setState(username, INVALID)
//        loginValidation.setState(password, INVALID)
//    }
//  }
//
//  @jfxf.FXML
//  def onLogout() {
//    loginPane.setVisible(true)
//    appPane.setDisable(true)
//  }
//
//  @jfxf.FXML
//  def onAlbumSelected() {
//  }
//
//  @jfxf.FXML
//  def onUpload() {
//  }
//
//  implicit def toSfxTableColumn[E, S](c: jfxs.control.TableColumn[E, S]) = new TableColumn(c)
//
//}
