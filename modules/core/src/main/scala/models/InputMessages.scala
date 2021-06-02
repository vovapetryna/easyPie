package models

import cats.implicits._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait InputMessages

object InputMessages {
  case class Simple(value: String)  extends InputMessages
  case class Wrong(ex: String)      extends InputMessages
  case class Close(message: String) extends InputMessages

  object Simple {
    implicit val r: Decoder[Simple] = deriveDecoder
    implicit val w: Encoder[Simple] = deriveEncoder
  }
  object Wrong {
    implicit val r: Decoder[Wrong] = deriveDecoder
    implicit val w: Encoder[Wrong] = deriveEncoder
  }
  object Close {
    implicit val r: Decoder[Close] = deriveDecoder
    implicit val w: Encoder[Close] = deriveEncoder
  }

  implicit val w: Encoder[InputMessages] = Encoder.instance {
    case simple @ Simple(_) => simple.asJson
    case wrong @ Wrong(_)   => wrong.asJson
    case close @ Close(_)   => close.asJson
  }
  implicit val r: Decoder[InputMessages] = List[Decoder[InputMessages]](
    Decoder[Simple].widen,
    Decoder[Wrong].widen,
    Decoder[Close].widen
  ).reduceLeft(_ or _)
}
