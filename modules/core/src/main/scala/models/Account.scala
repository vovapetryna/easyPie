package models

import io.circe._
import io.circe.refined._
import mongo4cats.circe._
import org.mongodb.scala.bson.ObjectId
import types._

case class Account(_id: ObjectId, login: NeString, token: NeString) {
  lazy val session: Account.Session = Account.Session(_id)
}

object Account {
  def fromRegister(register: Account.I.Register, token: NeString): Account = Account(new ObjectId(), register.login, token)

  implicit val r: Decoder[Account] = Decoder.forProduct3("_id", "login", "token")(Account.apply)
  implicit val e: Encoder[Account] = Encoder.forProduct3("_id", "login", "token")(t => (t._id, t.login, t.token))

  case class Session(_id: ObjectId)
  object Session {
    implicit val r: Decoder[Session] = Decoder.forProduct1("_id")(Session.apply)
    implicit val e: Encoder[Session] = Encoder.forProduct1("_id")(_._id)
  }

  object I {
    case class Login(login: NeString, token: NeString)
    object Login { implicit val r: Decoder[Login] = Decoder.forProduct2("login", "token")(Login.apply) }
    case class Register(login: NeString)
    object Register { implicit val r: Decoder[Register] = Decoder.forProduct1("login")(Register.apply) }
  }
}
