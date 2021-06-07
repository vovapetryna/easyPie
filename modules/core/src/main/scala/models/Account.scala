package models

import io.circe.generic.JsonCodec
import io.circe.refined._
import mongo4cats.circe._
import org.mongodb.scala.bson.ObjectId
import types._

@JsonCodec case class Account(_id: ObjectId, login: NeString, token: NeString) {
  lazy val session: Account.Session = Account.Session(_id)
}

object Account {
  def fromRegister(register: Account.I.Register, token: NeString): Account = Account(new ObjectId(), register.login, token)

  @JsonCodec case class Session(_id: ObjectId)

  object I {
    @JsonCodec(decodeOnly = true) case class Login(login: NeString, token: NeString)
    @JsonCodec(decodeOnly = true) case class Register(login: NeString)
  }
}
