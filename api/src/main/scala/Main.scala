import cats._
import cats.implicits._
import cats.effect._
import org.http4s.syntax._
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.Router
import doobie._

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {
  def run(args: List[String]) = for {
    xa <- IO(
      Transactor.fromDriverManager[IO](
        "org.postgresql.Driver",
        "jdbc:postgresql:pie",
        "postgres",
        "postgres"
      )
    )
    repo   = new doobie.Account(xa)
    route  = new routes.Account(repo)
    router = Router("/" -> route.routes).orNotFound
    server <- BlazeServerBuilder[IO](global).bindHttp(9001, "localhost").withHttpApp(router).resource.use(_ => IO.never).as(ExitCode.Success)
  } yield server

}
