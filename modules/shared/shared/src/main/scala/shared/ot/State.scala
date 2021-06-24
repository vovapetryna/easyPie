package shared.ot

import io.circe.generic.JsonCodec

import scala.scalajs.js.{annotation => jsA}

@JsonCodec
@jsA.JSExportTopLevel("State")
case class State(@jsA.JSExport("value") value: String, @jsA.JSExport("revision") revision: Int) {
  def next(value: String): State = State(value, revision + 1)
  def next: State                = State(value, revision + 1)
}

@jsA.JSExportTopLevel("state")
object State {
  @jsA.JSExport("empty")
  val empty: State = State("", 0)
}
