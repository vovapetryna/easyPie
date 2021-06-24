package repositories

import org.bson.types.ObjectId

trait OTDocument[F[_]] {
  def byIdNoOps(id: ObjectId): F[Option[models.Document]]
  def create(document: models.Document): F[ObjectId]
  def update(id: ObjectId, content: String, revision: Int, operation: shared.ot.Operation): F[Long]
  def byIdOps(id: ObjectId, fromRevision: Int, revision: Int): F[Option[models.Document]]
}
