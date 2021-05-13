package routes

import cats._
import cats.effect._
import cats.implicits._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import types._

final class Authentication[F[_]: Sync](
    repo: repositories.Account[F],
    authMiddleware: middlewares.Authentication[F, models.Session, models.Account]
) extends Http4sDsl[F] {
  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "authentication" / "logout" => authMiddleware.logout.run(())
    case req @ POST -> Root / "authentication" / "login" =>
      for {
        authEntry <- req.decodeJson[dtos.AuthEntry]
        account   <- repo.getByEmailAndPassword(authEntry.email, authEntry.password)
        session = account.map(_.session)
        response <- authMiddleware.login.run(session)
      } yield response
  }
}
