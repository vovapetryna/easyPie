package mongos

import cats.effect.Async
import cats.implicits._
import fs2.Stream
import mongo4cats.database.MongoCollectionF
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.{Filters => F}

class Permission[F[_]: Async](col: MongoCollectionF[models.Permission]) extends repositories.Permission[F] {
  def byAccountId(id: ObjectId): Stream[F, models.Permission] = col.find(F.eq("accountId", id)).stream
  def byPermission(permission: models.Permission): F[Option[models.Permission]] =
    col.find(F.and(F.eq("accountId", permission.accountId), F.eq("documentId", permission.documentId))).first.map(Option(_))
  def create(permission: models.Permission): F[Unit] = col.insertOne(permission).as()
}
