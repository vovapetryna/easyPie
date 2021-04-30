package routes

import cats.Applicative
import cats.effect.Async
import cats.implicits._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import types._

final class Account[F[_]: Async](repo: repositories.Account[F]) extends Http4sDsl[F] {
  implicit def encode[A[_]: Applicative]: EntityEncoder[A, models.Account] = jsonEncoderOf
  implicit def decode: EntityDecoder[F, models.Account]                    = jsonOf

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "account" / PathId(id) =>
      for {
        row  <- repo.getById(id)
        resp <- row.fold(NotFound())(Ok(_))
      } yield resp
    case req @ POST -> Root / "account" =>
      req.as[models.Account].flatMap { a =>
        for {
          cnt <- repo.insert(a)
          res <- cnt match {
            case 0 => NotFound()
            case _ => Ok()
          }
        } yield res
      }
  }
}
