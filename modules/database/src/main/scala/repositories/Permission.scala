package repositories

import fs2.Stream
import org.mongodb.scala.bson.ObjectId

trait Permission[F[_]] {
  def byAccountId(id: ObjectId): Stream[F, models.Permission]
  def byPermission(permission: models.Permission): F[Option[models.Permission]]
  def create(permission: models.Permission): F[Unit]
}
