import cats.effect._
import cats.implicits._
import eu.timepit.refined.auto._
import fs2._
import mongo4cats.client.MongoClientF
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import pureconfig._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = for {
    dbConf  <- IO { ConfigSource.default.at("db").loadOrThrow[configs.Db] }
    apiConf <- IO { ConfigSource.default.at("api").loadOrThrow[configs.Api] }
    result <- MongoClientF.fromConnectionString[IO](s"mongodb://${dbConf.host}:${dbConf.port}").use { client =>
      for {
        db         <- client.getDatabase(dbConf.db)
        collection <- db.getCollection(dbConf.collection)
        ref        <- Ref.of[IO, models.WState[IO]](models.WState.init[IO])
        exitCode <- {
          val ws = new routes.TestWS[IO](ref)
          val server = BlazeServerBuilder[IO](global)
            .bindHttp(apiConf.port, apiConf.host)
            .withHttpApp(ws.routes.orNotFound)
            .serve

          ref.get.flatMap { state =>
            val keepAlive = state.topics.values.map { topic =>
              Stream.awakeEvery[IO](30.seconds).map(_ => models.OutputMessages.Simple("ping")).through(topic.publish)
            }
            val queueProcess = state.queues.values.map { q => Stream.fromQueueUnterminated(q).map(println) }
            Stream(Seq(server) ++ keepAlive ++ queueProcess: _*).parJoinUnbounded.compile.drain.as(ExitCode.Success)
          }
        }
      } yield exitCode
    }
  } yield result
}
