package shared.actions

import shared.tree
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.implicits._

sealed trait Action

object Action {
  case class Add(value: String, left: tree.SAtom, right: tree.SAtom, siteId: Int) extends Action

  object Add {
    implicit val r: Decoder[Add] = deriveDecoder
    implicit val w: Encoder[Add] = deriveEncoder
  }

  implicit val w: Encoder[Action] = Encoder.instance {
    case add: Add => add.asJson
  }
  implicit val r: Decoder[Action] = List[Decoder[Action]](
    Decoder[Add].widen,
  ).reduceLeft(_ or _)
}
