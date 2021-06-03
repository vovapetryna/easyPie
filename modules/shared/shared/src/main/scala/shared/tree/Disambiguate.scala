package shared.tree

import cats.Order
import cats.implicits._
import io.circe._
import io.circe.generic.semiauto._

case class Disambiguate(id: Int, siteId: Int)

object Disambiguate {
  implicit val ordering: Order[Disambiguate] = (x: Disambiguate, y: Disambiguate) =>
    if (x.id === y.id) Order[Long].compare(x.siteId, y.siteId) else Order[Long].compare(x.id, y.id)

  implicit val r: Decoder[Disambiguate] = deriveDecoder
  implicit val w: Encoder[Disambiguate] = deriveEncoder
}
