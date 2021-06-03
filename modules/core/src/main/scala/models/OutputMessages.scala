package models

import cats.implicits._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait OutputMessages

object OutputMessages {
  case class Simple(value: String) extends OutputMessages
  case class Ping()                extends OutputMessages

  object Simple {
    implicit val r: Decoder[Simple] = deriveDecoder
    implicit val w: Encoder[Simple] = deriveEncoder
  }
  object Ping {
    implicit val r: Decoder[Ping] = deriveDecoder
    implicit val w: Encoder[Ping] = deriveEncoder
  }

  implicit val w: Encoder[OutputMessages] = Encoder.instance {
    case simple: Simple => simple.asJson
    case ping: Ping     => ping.asJson
  }
  implicit val r: Decoder[OutputMessages] = List[Decoder[OutputMessages]](
    Decoder[Simple].widen,
    Decoder[Ping].widen
  ).reduceLeft(_ or _)
}
