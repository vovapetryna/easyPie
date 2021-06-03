package configs

import eu.timepit.refined.pureconfig._
import pureconfig._
import pureconfig.generic.semiauto._
import types._

final case class Api(host: NeString, port: Int)
object Api { implicit val r: ConfigReader[Api] = deriveReader }

final case class Db(host: NeString, port: Int, db: NeString)
object Db { implicit val r: ConfigReader[Db] = deriveReader }

final case class Secure(jwtKey: NeString)
object Secure { implicit var r: ConfigReader[Secure] = deriveReader }
