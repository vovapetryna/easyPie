import cats.effect._
import com.typesafe.config.ConfigFactory
import doobie._
import eu.timepit.refined.auto._
import pureconfig._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val program = for {
      config <- IO {
        val cfg = ConfigFactory.load
        (ConfigSource.fromConfig(cfg).at("api").loadOrThrow[configs.Api], ConfigSource.fromConfig(cfg).at("database").loadOrThrow[configs.Db])
      }
      (_, dbCfg) = config
      tx <- Transactor.fromDriverManager[IO](dbCfg.driver, dbCfg.url, dbCfg.name, dbCfg.password)
      repo = new doobie.Account[IO](tx)
    } yield tx
  }
}
