package repositories

import org.mongodb.scala.bson.ObjectId
import types.NeString

trait Account[F[_]] {
  def byId(id: ObjectId): F[Option[models.Account]]
  def byLogin(login: NeString): F[Option[models.Account]]
  def create(account: models.Account): F[Unit]
}
