package routes

import cats.effect.Async
import cats.implicits._
import io.circe.syntax._
import mongo4cats.circe._
import org.http4s.AuthedRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class Document[F[_]: Async](repo: mongos.Repos[F]) extends Http4sDsl[F] {
  def routes: AuthedRoutes[models.Account, F] = AuthedRoutes.of[models.Account, F] {
    case GET -> Root / "get" as account =>
      Ok(account.asJson)
    case GET -> Root / "create" as account =>
      val tree = shared.tree.TreeDoc.init(account._id.hashCode())
      for {
        documentId <- repo.document.create(tree.rawTree)
        _          <- repo.permission.create(models.Permission(account._id, documentId))
        response   <- Ok(documentId.asJson)
      } yield response
  }
}
