package models

import cats.implicits._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait OutputMessages

object OutputMessages {
  case class Simple(value: String) extends OutputMessages

  object Simple {
    implicit val r: Decoder[Simple] = deriveDecoder
    implicit val w: Encoder[Simple] = deriveEncoder
  }

  implicit val w: Encoder[OutputMessages] = Encoder.instance { case simple @ Simple(_) => simple.asJson }
  implicit val r: Decoder[OutputMessages] = List[Decoder[OutputMessages]](Decoder[Simple].widen).reduceLeft(_ or _)
}
