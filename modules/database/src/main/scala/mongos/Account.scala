package mongos

import cats.effect.Async
import cats.implicits._
import mongo4cats.database.MongoCollectionF
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.{Filters => F}
import types.NeString

class Account[F[_]: Async](col: MongoCollectionF[models.Account]) extends repositories.Account[F] {
  def byId(id: ObjectId): F[Option[models.Account]] = col.find(F.eq("_id", id)).first.map(Option(_))

  def byLogin(login: NeString): F[Option[models.Account]] = col.find(F.eq("login", login.value)).first.map(Option(_))

  def create(account: models.Account): F[Unit] = col.insertOne(account).as()
}
