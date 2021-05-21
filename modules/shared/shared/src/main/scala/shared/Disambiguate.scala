package shared

import cats.Order
import cats.implicits._
import io.circe._
import io.circe.refined._
import types._

case class Disambiguate(id: Id, siteId: Id)

object Disambiguate {
  implicit val ordering: Order[Disambiguate] = (x: Disambiguate, y: Disambiguate) =>
    if (x.id === y.id) Order[Id].compare(x.siteId, y.siteId) else Order[Id].compare(x.id, y.id)

  implicit val r: Decoder[Disambiguate] = Decoder.forProduct2("id", "siteId")(Disambiguate.apply)
  implicit val w: Encoder[Disambiguate] = Encoder.forProduct2("id", "siteId")(t => (t.id, t.siteId))
}
