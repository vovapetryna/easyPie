package shared

import cats.Monoid
import cats.implicits._
import io.circe._
import io.circe.generic.semiauto._

case class Atom[T](id: Disambiguate, value: T)

object Atom {
  implicit def r[T](implicit rT: Decoder[T]): Decoder[Atom[T]] = deriveDecoder
  implicit def w[T](implicit wT: Encoder[T]): Encoder[Atom[T]] = deriveEncoder
}

case class Node[T](values: List[Atom[T]])

object Node {
  implicit def monoid[T]: Monoid[Node[T]] = new Monoid[Node[T]] {
    def empty: Node[T]                           = Node(List.empty[Atom[T]])
    def combine(x: Node[T], y: Node[T]): Node[T] = Node((x.values ++ y.values).sortBy(_.id))
  }
}
