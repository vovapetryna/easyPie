import cats.effect.{Async, Ref}
import cats.implicits._
import eu.timepit.refined.auto._
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.server.Router

import scala.concurrent.duration.DurationInt

package object routes {
  type TopicsR[F[_]] = Ref[F, models.Topics[F]]

  def init[F[_]: Async](repo: mongos.Repos[F], conf: configs.Conf): F[(HttpRoutes[F], Stream[F, Unit])] = {
    val authMiddleware = middlewares.Authentication.build[F](conf.secure.jwtKey, repo)

    val auth      = new routes.Auth[F](repo, authMiddleware)
    val documents = new routes.Document[F](repo)
    val files     = new routes.Resources[F]

    Ref.of[F, models.Topics[F]](models.Topics.init[F]).map { topic =>
      val ws = new routes.DocumentWS[F](topic, handlers.handler(), repo)
      Router(
        "/"        -> files.routes,
        "auth"     -> auth.routes,
        "document" -> authMiddleware.wrap(ws.routes),
        "document" -> authMiddleware.wrap(documents.routes)
      ) -> Stream
        .awakeEvery[F](40.seconds)
        .evalMap { _ =>
          topic.get.flatMap {
            _.topics.values.toList
              .map(_.publish1(models.OutputMessages.Ping(Set.empty, Set.empty)).as())
              .sequence
              .as()
          }
          }
    }
  }
}
