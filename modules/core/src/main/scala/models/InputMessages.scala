package models

import cats.implicits._
import io.circe.generic.JsonCodec

@JsonCodec(decodeOnly = true) sealed trait InputMessages

object InputMessages {
  @JsonCodec(decodeOnly = true) case class Simple(value: String)  extends InputMessages
  @JsonCodec(decodeOnly = true) case class Wrong(ex: String)      extends InputMessages
  @JsonCodec(decodeOnly = true) case class Close(message: String) extends InputMessages

  @JsonCodec(decodeOnly = true) case class Reload()                              extends InputMessages
  @JsonCodec(decodeOnly = true) case class Action(action: shared.actions.Action) extends InputMessages
}
