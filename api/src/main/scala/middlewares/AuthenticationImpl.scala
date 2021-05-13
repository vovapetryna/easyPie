package middlewares

import cats._
import cats.implicits._
import cats.data._
import cats.effect._
import org.http4s._
import org.http4s.implicits._
import org.http4s.dsl.io._
import org.http4s.circe._
import org.http4s.server._
import org.http4s.headers.Cookie
import io.circe.{parser, Decoder, Encoder}
import io.circe.syntax._

object AuthenticationImpl {
  def build[F[_]: Sync](secretKey: String, repo: repositories.Account[F]) = Authentication[F, models.Session, models.Account](
    Kleisli { session => repo.getById(session.id).map(o => Either.fromOption(o, "Failed to get account, wrong Id")) },
    secretKey
  )
}
