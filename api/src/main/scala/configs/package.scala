import cats.effect.Sync
import cats.implicits._
import eu.timepit.refined.pureconfig._
import pureconfig._
import pureconfig.generic.semiauto._
import types._

package object configs {
  final case class Api(host: NeString, port: Int)
  object Api { implicit val r: ConfigReader[Api] = deriveReader }

  final case class Db(host: NeString, port: Int, db: NeString)
  object Db { implicit val r: ConfigReader[Db] = deriveReader }

  final case class Secure(jwtKey: NeString)
  object Secure { implicit var r: ConfigReader[Secure] = deriveReader }

  final case class Conf(api: Api, db: Db, secure: Secure)

  def init[F[_]: Sync](resource: String, classLoader: ClassLoader): F[Conf] = for {
    db     <- Sync[F].delay { ConfigSource.resources(resource, classLoader).at("db").loadOrThrow[configs.Db] }
    api    <- Sync[F].delay { ConfigSource.resources(resource, classLoader).at("api").loadOrThrow[configs.Api] }
    secure <- Sync[F].delay { ConfigSource.resources(resource, classLoader).at("secure").loadOrThrow[configs.Secure] }
  } yield Conf(api, db, secure)
}
