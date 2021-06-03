package models

import io.circe._
import io.circe.generic.semiauto._
import mongo4cats.circe._
import org.mongodb.scala.bson.ObjectId

case class Permission(accountId: ObjectId, documentId: ObjectId)

object Permission {
  implicit val r: Decoder[Permission] = deriveDecoder
  implicit val w: Encoder[Permission] = deriveEncoder
}
