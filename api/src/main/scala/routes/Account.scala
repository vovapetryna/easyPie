package routes

import cats._
import cats.effect._
import cats.implicits._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import types._

final class Account[F[_]: Sync](repo: repositories.Account[F]) extends Http4sDsl[F] {
  val routes: AuthedRoutes[models.Account, F] = AuthedRoutes.of[models.Account, F] {
    case GET -> Root / "account" as account => Ok(account.asJson)
    case GET -> Root / "account" / PathId(id) as account =>
      for {
        row  <- repo.getById(id)
        resp <- row.fold(NotFound())(a => Ok(a.asJson))
      } yield resp
    case req @ POST -> Root / "account" as account =>
      req.req.decodeJson[models.Account].flatMap { a =>
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
