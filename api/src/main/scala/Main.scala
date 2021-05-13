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
  def run(args: List[String]) = for {
    api      <- jsConfig.loadConfig[IO, configs.Api](configs.Api.reader[IO], jsConfig.defFile(getClass.getClassLoader))
    db       <- jsConfig.loadConfig[IO, configs.Db](configs.Db.reader[IO], jsConfig.defFile(getClass.getClassLoader))
    authConf <- jsConfig.loadConfig[IO, configs.Auth](configs.Auth.reader[IO], jsConfig.defFile(getClass.getClassLoader))

    xa   = Transactor.fromDriverManager[IO](db.driver, db.url, db.name, db.password)
    repo = new doobie.Account(xa)
    auth = middlewares.AuthenticationImpl.build[IO](authConf.secretKey, repo)
    service = (new routes.Authentication(repo, auth).routes) <+>
      auth.wrap(new routes.Account(repo).routes)
    router = Router("/" -> service).orNotFound
    server <- BlazeServerBuilder[IO](global).bindHttp(api.port, api.host).withHttpApp(router).resource.use(_ => IO.never).as(ExitCode.Success)
  } yield server

}
