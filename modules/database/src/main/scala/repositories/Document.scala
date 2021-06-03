package repositories

import org.mongodb.scala.bson.ObjectId

trait Document[F[_]] {
  def byId(id: ObjectId): F[Option[shared.tree.STree]]
  def create(tree: shared.tree.STree): F[ObjectId]
}
