package mongos

import cats.effect.Async
import cats.implicits._
import mongo4cats.database.MongoCollectionF
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.{Filters => F, Projections => P, Updates => U}

class OTDocument[F[_]: Async](col: MongoCollectionF[models.Document]) extends repositories.OTDocument[F] {
  def byIdNoOps(id: ObjectId): F[Option[models.Document]] = col
    .find(F.eq("_id", id))
    .projection(P.slice("operations", -1))
    .first
    .map(Option(_))
  def create(document: models.Document): F[ObjectId] = col.insertOne(document).map(_.getInsertedId.asObjectId().getValue)
  def update(id: ObjectId, content: String, revision: Int, operation: shared.ot.Operation): F[Long] =
    col
      .updateOne(
        F.eq("_id", id),
        U.combine(
          U.push("operations", operation),
          U.set("revision", revision),
          U.set("content", content)
        )
      )
      .map(_.getModifiedCount)

  def byIdOps(id: ObjectId, fromRevision: Int, revision: Int): F[Option[models.Document]] = col
    .find(F.eq("_id", id))
    .projection(P.slice("operations", fromRevision, revision - fromRevision))
    .first
    .map(Option(_))
}
