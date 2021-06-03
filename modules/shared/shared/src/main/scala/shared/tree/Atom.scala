package shared.tree

import cats._
import cats.implicits._
import io.circe._
import io.circe.generic.semiauto._

@scala.scalajs.js.annotation.JSExportAll
case class Atom[T](id: Disambiguate, value: T)

object Atom {
  implicit def r[T](implicit rT: Decoder[T]): Decoder[Atom[T]] = deriveDecoder
  implicit def w[T](implicit wT: Encoder[T]): Encoder[Atom[T]] = deriveEncoder

  implicit def order[T]: Order[Atom[T]] = Order.by(_.id)

  def create[T](localId: Int, value: T)(implicit siteId: Int): Atom[T] = Atom(Disambiguate(localId, siteId), value)
}
