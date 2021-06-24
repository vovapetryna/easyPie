package models

import io.circe.generic.JsonCodec
import mongo4cats.circe._
import org.mongodb.scala.bson.ObjectId

@JsonCodec case class Document(_id: ObjectId, content: String, operations: List[shared.ot.Operation], revision: Int) {
  lazy val state: shared.ot.State = shared.ot.State(content, revision)
}

object Document { def empty: Document = Document(new ObjectId(), "", Nil, 0) }
