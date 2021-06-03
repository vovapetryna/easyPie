package middlewares

import cats.effect.Async
import cats.implicits._
import org.mongodb.scala.bson.ObjectId

object Permissions {
  def documentPermission[F[_]: Async, B](
      documentId: String
  )(account: models.Account)(body: => F[B])(or: => F[B])(implicit repo: mongos.Repos[F]): F[B] = {
    repo.permission.byPermission(models.Permission(account._id, new ObjectId(documentId))).flatMap {
      case Some(_) => body
      case None    => or
    }
  }
}
