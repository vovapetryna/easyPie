package routes

import cats.effect.Async
import cats.effect.std.Semaphore
import cats.implicits._
import fs2.Pipe
import fs2.concurrent.Topic
import io.circe.parser
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket._
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame._

class DocumentWS[F[_]: Async](
    topicsQ: TopicsQ[F],
    locksQ: LocksQ[F],
    handler: handlers.Handler[F, handlers.Input[F]],
    implicit val repo: mongos.Repos[F]
) extends Http4sDsl[F] {
  def routes: AuthedRoutes[models.Account, F] = AuthedRoutes.of[models.Account, F] { case GET -> Root / "edit" / documentId as account =>
    middlewares.Permissions.documentPermission(documentId)(account) {
      val toClientF = for {
        topics  <- topicsQ.take
        resultO <- topics.subscribe(documentId).pure[F]
        result <- resultO match {
          case Some(s) => (topics -> s).pure[F]
          case None =>
            Topic[F, shared.messages.Output].map(topic => topics.addTopic(documentId, topic))
        }
        (updatedTopics, stream) = result
        _ <- topicsQ.offer(updatedTopics)
      } yield stream
        .filter(r => if (r.to.isEmpty) true else r.to.contains(account.session._id.toHexString))
        .filter(r => !r.filter.contains(account.session._id.toHexString))
        .map(m => Text(m.asJson.noSpaces))

      val fromClient: Pipe[F, WebSocketFrame, Unit] = _.collect {
        case Text(msg, _) =>
          parser.parse(msg).flatMap(_.as[shared.messages.Input]) match {
            case Right(msg) => msg
            case Left(ex)   => shared.messages.Input.Wrong(ex.toString)
          }
        case _: Close => shared.messages.Input.Close("socket_closed")
        case _        => shared.messages.Input.Wrong("unsupported_message_type")
      }.evalMap(handler(handlers.Input(documentId, topicsQ, locksQ, repo, account.session))(_))

      val inboundSyncF = for {
        locks <- locksQ.take
        lockO <- locks.get(documentId).pure[F]
        updatedLocks <- lockO match {
          case Some(_) => locks.pure[F]
          case None    => Semaphore[F](1).map(lock => locks.addLock(documentId, lock))
        }
        result <- locksQ.offer(updatedLocks)
      } yield result

      for {
        toClient <- toClientF
        _        <- inboundSyncF
        result   <- WebSocketBuilder[F].build(toClient, fromClient)
      } yield result
    }(Forbidden("wrong_document_permissions"))
  }
}
