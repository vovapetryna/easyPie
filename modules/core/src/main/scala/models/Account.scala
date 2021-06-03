package models

import io.circe._
import io.circe.generic.semiauto._
import io.circe.refined._
import mongo4cats.circe._
import org.mongodb.scala.bson.ObjectId
import types._

case class Account(_id: ObjectId, login: NeString, token: NeString) {
  lazy val session: Account.Session = Account.Session(_id)
}

object Account {
  def fromRegister(register: Account.I.Register, token: NeString): Account = Account(new ObjectId(), register.login, token)

  implicit val r: Decoder[Account] = deriveDecoder
  implicit val e: Encoder[Account] = deriveEncoder

  case class Session(_id: ObjectId)
  object Session {
    implicit val r: Decoder[Session] = deriveDecoder
    implicit val e: Encoder[Session] = deriveEncoder
  }

  object I {
    case class Login(login: NeString, token: NeString)
    object Login { implicit val r: Decoder[Login] = deriveDecoder }
    case class Register(login: NeString)
    object Register { implicit val r: Decoder[Register] = deriveDecoder }
  }
}
