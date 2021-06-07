package routes

import cats.effect._
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.staticcontent._

class Resources[F[_]: Async] {
  private val srcRoot = "C:/Users/vovap/GitHub/easyPie_"
  def routes: HttpRoutes[F] =
    Router(
      "files" -> fileService[F](FileService.Config(s"$srcRoot/modules/shared/js/target/scala-2.13/shared-fastopt")),
      "files" -> fileService[F](FileService.Config(s"$srcRoot/app/src/main/js")),
      "files" -> fileService[F](FileService.Config(s"$srcRoot/app/src/main/html")),
      "files" -> fileService[F](FileService.Config(s"$srcRoot/app/src/main/css"))
    )
}
