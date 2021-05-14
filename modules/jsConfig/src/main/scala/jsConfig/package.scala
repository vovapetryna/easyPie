package jsConfig

import cats._
import cats.implicits._
import cats.data._
import cats.effect._
import io.circe._
import fs2._
import fs2.io.file
import java.io.File

def defFile(cl: ClassLoader) = new File(cl.getResource("application.conf").toURI)
type CReader = [F[_], A] =>> Reader[Map[String, Json], F[A]]

def deriveReader[F[_]: Sync, T](path: String)(implicit jDecoder: Decoder[T]): CReader[F, T] =
  Reader { m =>
    Sync[F].fromEither(for {
      json  <- m.get(path).toRight(new Exception(s"config_parse_error_no_such_path"))
      pased <- parser.decode[T](json.noSpaces).leftMap(e => new Exception(e.toString))
    } yield pased)
  }

def readConfig[F[_]: Sync](source: File)(implicit cs: ContextShift[F]) =
  Blocker[F].use { blocker =>
    file
      .readAll[F](source.toPath, blocker, 2048)
      .through(text.utf8Decode)
      .compile
      .toList
      .map(_.fold("")(_ + _))
  }

def loadConfig[F[_]: Sync, A](reader: CReader[F, A], source: File)(implicit blockerCS: ContextShift[F]) = for {
  sourceConfig <- readConfig(source)
  parsed <- Sync[F].fromEither {
    parser.decode[Map[String, Json]](sourceConfig).leftMap(e => new Exception(s"failed_to_parse_config_file"))
  }
  configData <- reader.run(parsed): F[A]
} yield configData
