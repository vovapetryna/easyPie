package models

import io.circe._
import io.circe.refined._
import types._

final case class Account(id: Id, name: NeString, email: Email, password: NeString) {
  lazy val session: Session = Session(id, email)
}

object Account {
  implicit val decoder: Decoder[Account] = Decoder.forProduct4("id", "name", "email", "password")(Account.apply)
  implicit val encoder: Encoder[Account] = Encoder.forProduct4("id", "name", "email", "password")(t => (t.id, t.name, t.email, t.password))
}
