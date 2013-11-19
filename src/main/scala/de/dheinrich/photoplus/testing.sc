import de.dheinrich.photoplus._
import java.io.File

/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 16.11.13
 * Time: 19:00
 * To change this template use File | Settings | File Templates.
 */

object Test {


  def timed[T](f: => T) = {
    val time = System.currentTimeMillis()
    val t = f
    println("took: " + (System.currentTimeMillis() - time))
    t
  }

  implicit class Func[T, A](f: T => A) {
    def flatThen[B](o: A => (T => B)) = (t: T) => o(f(t))(t)
  }

//  val f = new File("/home/daniel/Pictures/2012/08/21/IMG_4593_CR2.jpg")
//  val login = timed {
//    PicasaService.login("dannynullzwo", "xiao xiongmao")
//  }
//
//
//
//
//  login.right foreach {
//    implicit service =>
    //
    //      println(albums.size)

    //      timed {
    //        val pc = albums.mapValues(_.photos.size)
    //        for((album, count) <- pc if count > 0){
    //          println(s"Album: $album has $count photos")
    //        }
    //      }
//  }


  App.addJob(Job("test", Seq(new File("asd.jpg"))))


















































  App.jobs

}

