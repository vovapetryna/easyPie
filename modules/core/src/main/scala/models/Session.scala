package models

import types._
import io.circe._
import io.circe.refined._

case class Session(id: Id, email: Email)

object Session {
  implicit val r: Decoder[Session] = Decoder.forProduct2("id", "email")(Session.apply)
  implicit val w: Encoder[Session] = Encoder.forProduct2("id", "email")(t => (t.id, t.email))
}
