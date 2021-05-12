import cats._
import cats.implicits._
import cats.effect._
import cats.effect.implicits._
import org.http4s.syntax._
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.Router
import eu.timepit.refined.auto._
import doobie._

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {
  def run(args: List[String]) = for {
    apiWithDb <- jsConfig.loadConfig[IO, (configs.Api, configs.Db)](configs.apiWithDb[IO])
    (api, db) = apiWithDb
    xa        = Transactor.fromDriverManager[IO](db.driver, db.url, db.name, db.password)
    repo      = new doobie.Account(xa)
    router    = Router("/" -> (new routes.Account(repo)).routes).orNotFound
    server <- BlazeServerBuilder[IO](global).bindHttp(api.port, api.host).withHttpApp(router).resource.use(_ => IO.never).as(ExitCode.Success)
  } yield server

}
