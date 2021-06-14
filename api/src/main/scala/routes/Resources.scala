package routes

import cats.effect._
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.server.staticcontent._

class Resources[F[_]: Async] {
  private val srcRoot       = "C:/Users/vovap/GitHub/easyPie_"
  def routes: HttpRoutes[F] = fileService[F](FileService.Config(s"$srcRoot/app/src/main/react_app/build"))
}
