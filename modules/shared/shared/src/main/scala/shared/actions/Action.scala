package shared.actions

import cats.implicits._
import io.circe.generic.JsonCodec
import shared.tree

@JsonCodec sealed trait Action

object Action {
  @JsonCodec case class Add(value: String, left: tree.SAtom, right: tree.SAtom, siteId: Int) extends Action
}
