import cats.effect._
import cats.implicits._
import eu.timepit.refined.auto._
import fs2._
import mongo4cats.circe._
import mongo4cats.client.MongoClientF
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {
  middlewares.Authentication.tsecWindowsFix()

  def run(args: List[String]): IO[ExitCode] = for {
    conf <- configs.init[IO]("application.conf", getClass.getClassLoader)
    result <- MongoClientF
      .fromConnectionString[IO](s"mongodb+srv://${conf.db.name}:${conf.db.password}@${conf.db.host}")
      .use { client =>
        for {
          db      <- client.getDatabase(conf.db.db)
          repo    <- mongos.init[IO](db)
          routing <- routes.init[IO](repo, conf)
          (route, ping) = routing
          exitCode <- {
            val server = BlazeServerBuilder[IO](global)
              .bindHttp(conf.api.port, conf.api.host)
              .withHttpApp(route.orNotFound)
              .serve
            Stream(server, ping).parJoinUnbounded.compile.drain.as(ExitCode.Success)
          }
        } yield exitCode
      }
  } yield result
}
