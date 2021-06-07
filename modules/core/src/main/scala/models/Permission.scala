package models

import io.circe.generic.JsonCodec
import mongo4cats.circe._
import org.mongodb.scala.bson.ObjectId

@JsonCodec case class Permission(accountId: ObjectId, documentId: ObjectId)
