package repositories

import org.mongodb.scala.bson.ObjectId

trait Document[F[_]] {
  def byId(id: ObjectId): F[Option[shared.tree.TreeDoc]]
  def create(tree: shared.tree.TreeDoc): F[ObjectId]
  def update(id: ObjectId, tree: shared.tree.TreeDoc): F[Unit]
}
