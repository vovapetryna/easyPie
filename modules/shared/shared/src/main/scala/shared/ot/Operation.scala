package shared.ot

import cats.implicits._
import io.circe.generic.JsonCodec

import scala.scalajs.js.{annotation => jsA}

@JsonCodec final case class Operation(_t: String, pos: Int, sid: Int, value: String) {
  def isInsert: Boolean = _t === Operation.insertT
  def isDelete: Boolean = _t === Operation.deleteT
  def isId: Boolean     = _t === Operation.idT
}

@jsA.JSExportTopLevel("operation")
object Operation {
  val insertT: String = "insert"
  val deleteT: String = "delete"
  val idT: String     = "id"

  @jsA.JSExport("insert")
  def insert(position: Int, sid: Int, value: String): Operation = Operation(insertT, position, sid, value)
  @jsA.JSExport("delete")
  def delete(position: Int, sid: Int, value: String): Operation = Operation(deleteT, position, sid, value)
  @jsA.JSExport("id")
  def id: Operation = Operation(idT, -1, -1, "")
}
