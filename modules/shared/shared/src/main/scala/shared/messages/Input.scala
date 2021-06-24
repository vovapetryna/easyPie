package shared.messages

import cats.implicits._
import io.circe.generic.JsonCodec

@JsonCodec sealed trait Input

object Input {
  @JsonCodec case class Simple(value: String)  extends Input
  @JsonCodec case class Wrong(ex: String)      extends Input
  @JsonCodec case class Close(message: String) extends Input

  @JsonCodec case class Reload()                                                 extends Input
  @JsonCodec case class Operation(operation: shared.ot.Operation, revision: Int) extends Input
}
