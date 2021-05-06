import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import java.io.{File, FileInputStream, FileOutputStream}

object Main extends IOApp.Simple {
  def run = IO(println("Hello Effect"))
}
