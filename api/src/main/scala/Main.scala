import cats.effect._
import cats.implicits._
import eu.timepit.refined.auto._
import mongo4cats.client.MongoClientF
import pureconfig._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = for {
    dbConf <- IO { ConfigSource.default.at("db").loadOrThrow[configs.Db] }
    result <- MongoClientF.fromConnectionString[IO](s"mongodb://${dbConf.host}:${dbConf.port}").use { client =>
      for {
        db         <- client.getDatabase(dbConf.db)
        collection <- db.getCollection(dbConf.collection)
      } yield ExitCode.Success
    }
  } yield result
}
