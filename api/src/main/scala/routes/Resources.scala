package routes

import cats.effect._
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.server.staticcontent._

class Resources[F[_]: Async](conf: configs.Conf) {
  private val srcRoot = conf.env.env.value match {
    case "local"  => "C:/Users/vovap/GitHub/easyPie_/app/src/main/react_app"
    case "docker" => "/opt/docker"
  }
  def routes: HttpRoutes[F] = fileService[F](FileService.Config(s"$srcRoot/build"))
}
