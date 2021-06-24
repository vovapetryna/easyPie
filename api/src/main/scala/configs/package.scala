import cats.effect.Sync
import cats.implicits._
import eu.timepit.refined.pureconfig._
import pureconfig._
import pureconfig.generic.semiauto._
import types._

package object configs {
  final case class Api(host: NeString, port: Int)
  object Api { implicit val r: ConfigReader[Api] = deriveReader }

  final case class Db(host: NeString, name: NeString, password: NeString, db: NeString)
  object Db { implicit val r: ConfigReader[Db] = deriveReader }

  final case class Secure(jwtKey: NeString)
  object Secure { implicit val r: ConfigReader[Secure] = deriveReader }

  final case class Env(env: NeString)
  object Env { implicit val r: ConfigReader[Env] = deriveReader }

  final case class Conf(api: Api, db: Db, secure: Secure, env: Env)

  def init[F[_]: Sync](resource: String, classLoader: ClassLoader): F[Conf] = for {
    db     <- Sync[F].delay { ConfigSource.resources(resource, classLoader).at("db").loadOrThrow[configs.Db] }
    api    <- Sync[F].delay { ConfigSource.resources(resource, classLoader).at("api").loadOrThrow[configs.Api] }
    secure <- Sync[F].delay { ConfigSource.resources(resource, classLoader).at("secure").loadOrThrow[configs.Secure] }
    env    <- Sync[F].delay { ConfigSource.resources(resource, classLoader).at("env").loadOrThrow[configs.Env] }
  } yield Conf(api, db, secure, env)
}
