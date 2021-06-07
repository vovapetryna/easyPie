package models

import cats.implicits._
import io.circe.generic.JsonCodec

@JsonCodec(encodeOnly = true) sealed trait OutputMessages {
  val to: Set[String]
  val filter: Set[String]
}

object OutputMessages {
  @JsonCodec(encodeOnly = true) case class Simple(value: String, to: Set[String], filter: Set[String])                 extends OutputMessages
  @JsonCodec(encodeOnly = true) case class Ping(to: Set[String], filter: Set[String])                                  extends OutputMessages
  @JsonCodec(encodeOnly = true) case class Reload(value: shared.tree.TreeDoc, to: Set[String], filter: Set[String])    extends OutputMessages
  @JsonCodec(encodeOnly = true) case class Action(action: shared.actions.Action, to: Set[String], filter: Set[String]) extends OutputMessages
}
