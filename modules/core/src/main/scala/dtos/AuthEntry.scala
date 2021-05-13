package dtos

import types._
import io.circe._
import io.circe.refined._

final case class AuthEntry(email: Email, password: NeString)

object AuthEntry {
  implicit val r: Decoder[AuthEntry] = Decoder.forProduct2("email", "password")(AuthEntry.apply)
  implicit val w: Encoder[AuthEntry] = Encoder.forProduct2("email", "password")(t => (t.email, t.password))
}
