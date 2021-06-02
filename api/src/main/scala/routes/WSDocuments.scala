package routes

import cats.effect.Async
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

class WSDocuments[F[_]: Async](topicsR: TopicsR[F], handler: types.Handler[F, handlers.Input[F]]) extends Http4sDsl[F] {
  def routes: HttpRoutes[F] = HttpRoutes.of { case GET -> Root / "document" / IntVar(documentId) =>
    val toClientF = for {
      topics  <- topicsR.get
      resultO <- topics.subscribe(documentId).pure[F]
      result <- resultO match {
        case Some(s) => s.pure[F]
        case None    => Topic[F, models.OutputMessages].flatMap(topic => topicsR.modify(_.addTopic(documentId, topic)))
      }
    } yield result.map(m => Text(m.asJson.noSpaces))

    val fromClient: Pipe[F, WebSocketFrame, Unit] = _.collect {
      case Text(msg, _) =>
        parser.parse(msg).flatMap(_.as[models.InputMessages]) match {
          case Right(msg) => msg
          case Left(ex)   => models.InputMessages.Wrong(ex.toString)
        }
      case _: Close => models.InputMessages.Close("socket_closed")
      case _        => models.InputMessages.Wrong("unsupported_message_type")
    }.evalMap(handler(handlers.Input(documentId, topicsR))(_))

    toClientF.flatMap(toClient => WebSocketBuilder[F].build(toClient, fromClient))
  }
}
