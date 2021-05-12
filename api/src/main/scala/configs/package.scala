package configs

import cats._
import cats.implicits._
import cats.effect._
import types._
import io.circe._
import io.circe.refined._
import jsConfig._

final case class Api(host: NeString, port: Int)
object Api {
  implicit val r: Decoder[Api]            = Decoder.forProduct2("host", "port")(Api.apply)
  def reader[F[_]: Sync]: CReader[F, Api] = deriveReader[F, Api]("api")
}

final case class Db(driver: NeString, url: Url, name: NeString, password: NeString)
object Db {
  implicit val r: Decoder[Db]            = Decoder.forProduct4("driver", "url", "name", "password")(Db.apply)
  def reader[F[_]: Sync]: CReader[F, Db] = deriveReader[F, Db]("db")
}

def apiWithDb[F[_]: Sync]: CReader[F, (Api, Db)] = for {
  api <- Api.reader
  db  <- Db.reader
} yield (api, db).tupled
