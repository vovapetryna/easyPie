package routes

import cats.effect.Async
import cats.implicits._
import io.circe.syntax._
import org.http4s.AuthedRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class Documents[F[_]: Async] extends Http4sDsl[F] {
  def routes: AuthedRoutes[models.Account, F] = AuthedRoutes.of[models.Account, F] { case GET -> Root / "get" as account =>
    Ok(account.asJson)
  }
}
