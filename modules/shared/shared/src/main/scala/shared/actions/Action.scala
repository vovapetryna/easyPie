package shared.actions

import cats.implicits._
import io.circe.generic.JsonCodec
import io.circe.syntax._
import shared.tree

@JsonCodec sealed trait Action

@scala.scalajs.js.annotation.JSExportTopLevel("ActionUtil")
object Action {
  @JsonCodec case class Add(value: String, left: tree.SAtom, right: tree.SAtom, siteId: Int) extends Action
  @JsonCodec case class Nothing()                                                            extends Action

  def nothing: Action = Nothing()

  @scala.scalajs.js.annotation.JSExport("toJsonString")
  def toJsonString(action: Action): String = {
    action.asJson.noSpaces
  }
}
