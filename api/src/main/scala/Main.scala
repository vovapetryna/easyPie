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

import java.io.File

object Main extends IOApp {
  val confFile = jsConfig.defFile(getClass.getClassLoader)

  def run(args: List[String]) = for {
    api <- jsConfig.loadConfig[IO, configs.Api](configs.Api.reader[IO], confFile)
    db  <- jsConfig.loadConfig[IO, configs.Db](configs.Db.reader[IO], confFile)
    tx      = Transactor.fromDriverManager[IO](db.driver, db.url, db.name, db.password)
    repos   = doobies.repos(tx)
    service = new routes.Account(repos.account).routes
    router  = Router("/" -> service).orNotFound
    server <- BlazeServerBuilder[IO](global).bindHttp(api.port, api.host).withHttpApp(router).resource.use(_ => IO.never).as(ExitCode.Success)
  } yield server

}
