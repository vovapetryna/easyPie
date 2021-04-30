import eu.timepit.refined.pureconfig._
import pureconfig._
import types._

package object configs {
  final case class Api(host: NeString, port: Int)
  object Api {
    implicit val reader: ConfigReader[Api] = ConfigReader.forProduct2("host", "port")(Api.apply)
  }

  final case class Db(driver: NeString, url: Url, name: NeString, password: NeString)
  object Db {
    implicit val reader: ConfigReader[Db] = ConfigReader.forProduct4("driver", "url", "name", "password")(Db.apply)
  }
}
