import cats._
import cats.data._
import cats.implicits._
import io.circe._
import io.circe.syntax._
import cats.effect._
import cats.effect.implicits._
import fs2._
import fs2.io.file

import java.io.File

type CReader = [F[_], A] =>> Reader[Map[String, Json], F[A]]

case class DbConf(driver: String, url: String, name: String, password: String)
object DbConf { implicit val r: Decoder[DbConf] = Decoder.forProduct4("driver", "url", "name", "password")(DbConf.apply) }
case class ServerConf(host: String, port: Int)
object ServerConf { implicit val r: Decoder[ServerConf] = Decoder.forProduct2("host", "port")(ServerConf.apply) }

def deriveReader[F[_]: Sync, T](path: String)(implicit jDecoder: Decoder[T]): CReader[F, T] =
  Reader{ m => 
    Sync[F].fromEither(for {
      json <- m.get(path).toRight(new Exception(s"failed to parse config [ no such path $path ]"))
      pased <- parser.decode[T](json.noSpaces).leftMap(e => new Exception(e.toString))
    } yield pased)
  }

// --- read config with fs2 io file readAll
def readConfig[F[_]: Sync](source: File)(implicit cs: ContextShift[F]) =
  Blocker[F].use { blocker =>
    file
      .readAll[F](source.toPath, blocker, 2048)
      .through(text.utf8Decode)
      .compile
      .toList
      .map(_.fold("")(_ + _))
  }

def loadConfig[F[_]: Sync, A](source: File, reader: CReader[F, A])(implicit blockerCS: ContextShift[F]) = for {
  sourceConfig <- readConfig(source)
  parsed       <- Sync[F].fromEither(parser.decode[Map[String, Json]](sourceConfig).leftMap(e => new Exception(s"failed to parse config file : ${e.toString}")))
  configData   <- reader.run(parsed): F[A]
} yield configData

//test space
{
  implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
  val source                        = new File("C:/Users/vovap/GitHub/easyPie/api/src/main/resources/application.conf")
  val reader = for {
    db     <- deriveReader[IO, DbConf]("db")
    server <- deriveReader[IO, ServerConf]("api")
  } yield (db, server).tupled

  val program = loadConfig[IO, (DbConf, ServerConf)](source, reader)

  program.unsafeRunSync()
}
