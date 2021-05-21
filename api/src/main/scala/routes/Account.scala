package routes

import cats._
import cats.effect._
import cats.implicits._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.implicits._
import types._

final class Account[F[_]: Async](repo: repositories.Account[F]) extends Http4sDsl[F] {
  implicit val aDecoder = jsonOf[F, models.Account]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "account" / PathId(id) =>
      for {
        row  <- repo.getById(id)
        resp <- row.fold(NotFound())(a => Ok(a.asJson))
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
