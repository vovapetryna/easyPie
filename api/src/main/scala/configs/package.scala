import types._

package object configs {
  final case class Api(host: NeString, port: Int)
  object Api {}

  final case class Db(driver: NeString, url: Url, name: NeString, password: NeString)
  object Db {}
}
