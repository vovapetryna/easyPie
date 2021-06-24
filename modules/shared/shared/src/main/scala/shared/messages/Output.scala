package shared.messages

import cats.implicits._
import io.circe.generic.JsonCodec

@JsonCodec sealed trait Output {
  val to: Set[String]
  val filter: Set[String]
}

object Output {
  @JsonCodec case class Simple(value: String, to: Set[String], filter: Set[String])                     extends Output
  @JsonCodec case class Ping(to: Set[String], filter: Set[String])                                      extends Output
  @JsonCodec case class Reload(state: shared.ot.State, to: Set[String], filter: Set[String])            extends Output
  @JsonCodec case class Operation(operation: shared.ot.Operation, to: Set[String], filter: Set[String]) extends Output
  @JsonCodec case class Ack(to: Set[String], filter: Set[String])                                       extends Output
}
