package configs

import types._
import eu.timepit.refined.pureconfig._
import pureconfig._

final case class Api(host: NeString, port: Int)
object Api {
  implicit val r: ConfigReader[Api] = ConfigReader.forProduct2("host", "port")(Api.apply)
}

final case class Db(host: NeString, port: Int, db: NeString, collection: NeString)
object Db {
  implicit val r: ConfigReader[Db] = ConfigReader.forProduct4("host", "port", "db", "collection")(Db.apply)
}
