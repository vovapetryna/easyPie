import cats.effect._
import cats.implicits._
import eu.timepit.refined.auto._
import fs2._
import mongo4cats.circe._
import mongo4cats.client.MongoClientF
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import pureconfig._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt

object Main extends IOApp {
  middlewares.Authentication.tsecWindowsFix()

  def run(args: List[String]): IO[ExitCode] = for {
    dbConf  <- IO { ConfigSource.default.at("db").loadOrThrow[configs.Db] }
    apiConf <- IO { ConfigSource.default.at("api").loadOrThrow[configs.Api] }
    secConf <- IO { ConfigSource.default.at("secure").loadOrThrow[configs.Secure] }
    result <- MongoClientF.fromConnectionString[IO](s"mongodb://${dbConf.host}:${dbConf.port}").use { client =>
      for {
        db         <- client.getDatabase(dbConf.db)
        accountCol <- db.getCollectionWithCirceCodecs[models.Account]("accounts")
        accountRepo    = new mongos.Account[IO](accountCol)
        authMiddleware = middlewares.Authentication.build[IO](secConf.jwtKey, accountRepo)
        topicsR <- Ref.of[IO, models.Topics[IO]](models.Topics.init[IO])
        exitCode <- {
          import org.http4s.server.Router
          val auth      = new routes.Auth[IO](accountRepo, authMiddleware)
          val documents = new routes.Documents[IO]
          val ws        = new routes.WSDocuments[IO](topicsR, handlers.handler())
          val allRoutes = Router(
            "/"        -> authMiddleware.wrap(ws.routes),
            "auth"     -> auth.routes,
            "document" -> authMiddleware.wrap(documents.routes)
          ).orNotFound
          val server = BlazeServerBuilder[IO](global)
            .bindHttp(apiConf.port, apiConf.host)
            .withHttpApp(allRoutes)
            .serve
          val ping = Stream
            .awakeEvery[IO](30.seconds)
            .evalMap(_ => topicsR.get.flatMap(s => s.topics.values.toList.map(_.publish1(models.OutputMessages.Ping())).sequence))
          Stream(server, ping).parJoinUnbounded.compile.drain.as(ExitCode.Success)
        }
      } yield exitCode
    }
  } yield result
}
