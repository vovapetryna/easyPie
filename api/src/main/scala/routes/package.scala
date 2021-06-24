import cats.effect.Async
import cats.effect.std.Queue
import cats.implicits._
import eu.timepit.refined.auto._
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.server.Router

import scala.concurrent.duration.DurationInt

package object routes {
  type TopicsQ[F[_]] = Queue[F, models.Topics[F]]
  type LocksQ[F[_]]  = Queue[F, models.Locks[F]]

  def init[F[_]: Async](repo: mongos.Repos[F], conf: configs.Conf): F[(HttpRoutes[F], Stream[F, Unit])] = {
    val authMiddleware = middlewares.Authentication.build[F](conf.secure.jwtKey, repo)

    val auth      = new routes.Auth[F](repo, authMiddleware)
    val documents = new routes.Document[F](repo)
    val files     = new routes.Resources[F]

    for {
      topicsQ <- Queue.bounded[F, models.Topics[F]](1)
      _       <- topicsQ.offer(models.Topics.init[F])
      locksQ  <- Queue.bounded[F, models.Locks[F]](1)
      _       <- locksQ.offer(models.Locks.init[F])
    } yield {
      val ws = new routes.DocumentWS[F](topicsQ, locksQ, handlers.handler(), repo)
      Router(
        "/"        -> files.routes,
        "auth"     -> auth.routes,
        "document" -> authMiddleware.wrap(ws.routes),
        "document" -> authMiddleware.wrap(documents.routes)
      ) -> Stream
        .awakeEvery[F](20.seconds)
        .evalMap { _ =>
          for {
            topics <- topicsQ.take
            _      <- topicsQ.offer(topics)
            result <- topics.topics.values.toList
              .map(_.publish1(shared.messages.Output.Ping(Set.empty, Set.empty)).as())
              .sequence
              .as()
          } yield result
        }
    }
  }
}
