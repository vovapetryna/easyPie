package routes

import cats.effect.Async
import cats.implicits._
import io.circe.refined._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class Auth[F[_]: Async](repo: mongos.Repos[F], authMiddleware: middlewares.Authentication[F, models.Account.Session, models.Account])
    extends Http4sDsl[F] {
  def routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "logout" => authMiddleware.logout

    case req @ POST -> Root / "login" =>
      req.asJsonDecode[models.Account.I.Login].flatMap { login =>
        for {
          account <- repo.account.byLogin(login.login)
          session = account.map(_.session)
          valid    <- middlewares.Authentication.validatePass(login.token, account.map(_.token))
          response <- authMiddleware.login(valid, session)
        } yield response
      }

    case req @ POST -> Root / "register" =>
      req.asJsonDecode[models.Account.I.Register].flatMap { register =>
        for {
          token <- middlewares.Authentication.generateToken
          hash  <- middlewares.Authentication.encodePass(token)
          account = models.Account.fromRegister(register, hash)
          inserted <- repo.account.create(account)
          response <- if (inserted === 1) Ok(token.asJson) else BadRequest("account_insert_error")
        } yield response
      }
  }
}
