package routes

import cats.effect.std.Queue
import cats.effect.{Async, Ref}
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

class TestWS[F[_]: Async](wState: Ref[F, models.WState[F]]) extends Http4sDsl[F] {
  def routes: HttpRoutes[F] = HttpRoutes.of { case GET -> Root / "document" / IntVar(documentId) =>
    val toClientF = for {
      state   <- wState.get
      resultO <- state.subscribe(documentId).pure[F]
      result <- resultO match {
        case Some(s) => s.pure[F]
        case None    => Topic[F, models.OutputMessages].flatMap(topic => wState.modify(_.addTopic(documentId, topic)))
      }
    } yield result.map(m => Text(m.asJson.noSpaces))

    def marshaller(queue: Queue[F, models.InputMessages]): Pipe[F, WebSocketFrame, Unit] = _.collect {
      case Text(msg, _) =>
        parser.parse(msg).flatMap(_.as[models.InputMessages]) match {
          case Right(msg) => msg
          case Left(ex)   => models.InputMessages.Wrong(ex.toString)
        }
      case _: Close => models.InputMessages.Close("socket_closed")
      case _        => models.InputMessages.Wrong("unsupported_message_type")
    }.evalMap(queue.offer)

    val fromClientF: F[Pipe[F, WebSocketFrame, Unit]] = for {
      state  <- wState.get
      queueO <- state.getQueue(documentId).pure[F]
      queue <- queueO match {
        case Some(q) => q.pure[F]
        case None    => Queue.unbounded[F, models.InputMessages].flatMap(q => wState.modify(_.addQueue(documentId, q)))
      }
    } yield marshaller(queue)

    for {
      toClient   <- toClientF
      fromClient <- fromClientF
      response   <- WebSocketBuilder[F].build(toClient, fromClient)
    } yield response
  }
}
