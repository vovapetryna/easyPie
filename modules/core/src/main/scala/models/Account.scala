package models

import io.circe._
import io.circe.refined._
import types._

case class Account(id: Id, name: NeString, email: Email, password: NeString)

object Account {
  implicit val decoder: Decoder[Account] = Decoder.forProduct4("id", "name", "email", "password")(Account.apply)
  implicit val encoder: Encoder[Account] = Encoder.forProduct4("id", "name", "email", "password")(t => (t.id, t.name, t.email, t.password))
}
