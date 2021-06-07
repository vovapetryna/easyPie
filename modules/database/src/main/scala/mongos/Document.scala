package mongos

import cats.effect.Async
import cats.implicits._
import mongo4cats.database.MongoCollectionF
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.{Filters => F}

class Document[F[_]: Async](col: MongoCollectionF[shared.tree.TreeDoc]) extends repositories.Document[F] {
  def byId(id: ObjectId): F[Option[shared.tree.TreeDoc]] = col.find(F.eq("_id", id)).first.map(Option(_))

  def create(tree: shared.tree.TreeDoc): F[ObjectId] = col.insertOne(tree).map(_.getInsertedId.asObjectId().getValue)

  def update(id: ObjectId, tree: shared.tree.TreeDoc): F[Unit] = col.findOneAndReplace(F.eq("_id", id), tree).as()
}
