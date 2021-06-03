package middlewares

import cats.data.{EitherT, Kleisli, OptionT}
import cats.effect.Sync
import cats.implicits._
import eu.timepit.refined.auto._
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Cookie
import org.http4s.server.AuthMiddleware
import tsec.common.SecureRandomIdGenerator
import tsec.jws.mac.JWTMac
import tsec.jwt.JWTClaims
import tsec.mac.jca._
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt
import types._

case class Authentication[F[_]: Sync, Session: Decoder: Encoder, Account](
    retrieveAccount: Session => F[Either[String, Account]],
    secretKey: String,
    cookieKey: String = "auth"
) extends Http4sDsl[F] {
  val keyF: F[MacSigningKey[HMACSHA256]] = HMACSHA256.buildKey[F](secretKey.getBytes)

  def login(valid: Option[Boolean], s: Option[Session]): F[Response[F]] = s match {
    case Some(s) if valid.exists(_ === true) =>
      for {
        key <- keyF
        claim = JWTClaims(customFields = Seq(cookieKey -> s.asJson))
        token    <- JWTMac.buildToString[F, HMACSHA256](claim, key)
        response <- Ok().map(_.addCookie(ResponseCookie(cookieKey, token, path = Option("/"))))
      } yield response
    case _ => NotFound("account_not_found")
  }

  val logout: F[Response[F]] = Ok().map(_.addCookie(ResponseCookie(cookieKey, "", path = Option("/"))))

  private def authUser: Kleisli[F, Request[F], Either[String, Account]] =
    Kleisli { req =>
      val data = for {
        key     <- EitherT.right[String](keyF)
        header  <- EitherT.fromEither[F](req.headers.get[Cookie].toRight("cookie_parse_error"))
        cookie  <- EitherT.fromEither[F](header.values.find(_.name === cookieKey).toRight("auth_cookie_not_found"))
        token   <- EitherT.right[String](JWTMac.verifyAndParse[F, HMACSHA256](cookie.content, key))
        session <- EitherT.fromEither[F](token.body.getCustom[Session](cookieKey).leftMap(_.toString))
      } yield session

      val account = for {
        session <- data
        account <- EitherT(retrieveAccount(session))
      } yield account

      account.value
    }

  private def onFailure: AuthedRoutes[String, F] = Kleisli(error => OptionT.liftF(Forbidden(error.context)))

  val wrap: AuthMiddleware[F, Account] = AuthMiddleware(authUser, onFailure)
}

object Authentication {
  lazy val generator: SecureRandomIdGenerator = SecureRandomIdGenerator(32)

  def generateToken[F[_]: Sync]: F[NeString]              = generator.generateF[F].map(NeString.unsafeFrom)
  def encodePass[F[_]: Sync](pass: NeString): F[NeString] = BCrypt.hashpw[F](pass).map(NeString.unsafeFrom)
  def validatePass[F[_]: Sync](pass: NeString, hash: Option[NeString]): F[Option[Boolean]] =
    hash.map(h => BCrypt.checkpwBool[F](pass, PasswordHash.apply[BCrypt](h))).sequence

  def build[F[_]: Sync](secretKey: String, repo: mongos.Repos[F]): Authentication[F, models.Account.Session, models.Account] =
    Authentication[F, models.Account.Session, models.Account](
      session => repo.account.byId(session._id).map(o => Either.fromOption(o, "account_get_error_wrong_id")),
      secretKey
    )

  def tsecWindowsFix(): Unit =
    try {
      java.security.SecureRandom.getInstance("NativePRNGNonBlocking")
      ()
    } catch {
      case _: java.security.NoSuchAlgorithmException =>
        val secureRandom                = new java.security.SecureRandom()
        val defaultSecureRandomProvider = secureRandom.getProvider.get(s"SecureRandom.${secureRandom.getAlgorithm}")
        secureRandom.getProvider.put("SecureRandom.NativePRNGNonBlocking", defaultSecureRandomProvider)
        java.security.Security.addProvider(secureRandom.getProvider)
        ()
    }
}
