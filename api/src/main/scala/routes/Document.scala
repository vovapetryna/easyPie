package routes

import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import io.circe.syntax._
import fs2._
import mongo4cats.circe._
import org.http4s.AuthedRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.mongodb.scala.bson.ObjectId

class Document[F[_]: Async](repo: mongos.Repos[F]) extends Http4sDsl[F] {
  def routes: AuthedRoutes[models.Account, F] = AuthedRoutes.of[models.Account, F] {
    case GET -> Root / "get" as account =>
      Ok(account.asJson)
    case GET -> Root / "create" as account =>
      val tree = shared.tree.TreeDoc.init(0)
      for {
        documentId <- repo.document.create(tree)
        _          <- repo.permission.create(models.Permission(account._id, documentId))
        response   <- Ok(documentId.asJson)
      } yield response
    case GET -> Root / "list" as account =>
      val permissions = repo.permission.byAccountId(account._id)
      Ok(Stream("[") ++ permissions.map(_.documentId.asJson.noSpaces).intersperse(",") ++ Stream("]"))
    case GET -> Root / "join" / documentId as account =>
      val id = new ObjectId(documentId)
      val permissionT = for {
        document <- OptionT(repo.document.byId(id))
        _        <- OptionT.liftF(repo.permission.create(models.Permission(account._id, id)))
        response <- OptionT.liftF(Ok())
      } yield response
      permissionT.value.flatMap {
        case Some(response) => Async[F].pure(response)
        case None           => BadRequest()
      }
  }
}
