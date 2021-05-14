package middlewares

import cats._
import cats.implicits._
import cats.data._
import cats.effect._
import org.http4s._
import org.http4s.implicits._
import org.http4s.dsl.io._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import org.http4s.server._
import org.http4s.headers.Cookie
import io.circe.{parser, Decoder, Encoder}
import io.circe.syntax._
import pdi.jwt.{Jwt, JwtAlgorithm}

import java.time.Clock

case class Authentication[F[_]: Sync, Session: Decoder: Encoder, Account](
    retrieveAccount: Session => F[Either[String, Account]],
    secretKey: String,
    cookieKey: String = "auth"
) extends Http4sDsl[F] {
  implicit val clock: Clock = Clock.systemUTC

  def login(s: Option[Session])(implicit sE: Encoder[Session]): F[Response[F]] = s match {
    case Some(session) =>
      val token = Jwt.encode(session.asJson.noSpaces, secretKey, JwtAlgorithm.HS256)
      Ok().map(_.addCookie(ResponseCookie(cookieKey, token, path = Option("/"))))
    case None => NotFound()
  }

  val logout: F[Response[F]] = Ok().map(_.addCookie(ResponseCookie(cookieKey, "", path = Option("/"))))

  private def authUser(implicit sD: Decoder[Session]): Kleisli[F, Request[F], Either[String, Account]] =
    Kleisli { req =>
      val data = for {
        header  <- req.headers.get[Cookie].toRight("cookie_parse_error")
        cookie  <- header.values.find(_.name === cookieKey).toRight("auth_cookie_not_found")
        token   <- Jwt.decodeRaw(cookie.content, secretKey, Seq(JwtAlgorithm.HS256)).toEither.leftMap(_.toString)
        session <- parser.decode[Session](token).leftMap(_.toString)
      } yield session

      val account = for {
        session <- EitherT.fromEither[F](data)
        account <- EitherT(retrieveAccount(session))
      } yield account

      account.value
    }

  private def onFailure: AuthedRoutes[String, F] = Kleisli(error => OptionT.liftF(Forbidden(error.context)))

  val wrap = AuthMiddleware(authUser, onFailure)
}
