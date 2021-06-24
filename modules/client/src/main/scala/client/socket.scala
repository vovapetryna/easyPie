package client

import cats.effect.std.{Dispatcher, Queue}
import cats.effect.{Async, Resource}
import cats.implicits._
import fs2.{Pipe, Stream}
import io.circe.Encoder
import io.circe.parser.decode
import io.circe.syntax._
import org.scalajs.dom

object socket {
  type EitherOutput = Either[Throwable, shared.messages.Output]
  case object SocketClosing          extends Exception("socket_is_closing")
  case object SocketClosed           extends Exception("socket_is_closed")
  case object SocketOpening          extends Exception("socket_is_opening")
  case class SocketSend(msg: String) extends Exception(msg)

  def simplePipe[F[_]: Async]: Pipe[F, EitherOutput, Unit] = inS => inS.evalMap(msg => Async[F].delay(println(msg)))

  def errorHandlingPipe[F[_]: Async](errorF: String => F[Unit]): Pipe[F, EitherOutput, shared.messages.Output] = inS =>
    inS
      .evalMap[F, EitherOutput] {
        case m @ Left(ex) => errorF(ex.toString).as(m)
        case m            => Async[F].delay(m)
      }
      .collect { case Right(msg) => msg }

  def secureSend[F[_]: Async, T](ws: dom.WebSocket, msg: T)(implicit w: Encoder[T]): F[Unit] =
    ws.readyState match {
      case 0 => Async[F].raiseError(SocketOpening)
      case 1 => Async[F].delay(ws.send(msg.asJson.noSpaces)).adaptError(ex => SocketSend(ex.getMessage))
      case 2 => Async[F].raiseError(SocketClosing)
      case 3 => Async[F].raiseError(SocketClosed)
    }

  def registerSocket[F[_]: Async](
      host: String,
      inputPipe: Pipe[F, EitherOutput, Unit],
      outputStream: Stream[F, shared.messages.Input]
  ): F[Unit] =
    Resource.make[F, dom.WebSocket](Async[F].delay(new dom.WebSocket(host)))(ws => Async[F].delay(ws.close())).use { ws =>
      val onCloseF = Async[F].async_[Unit](cb => ws.onclose = _ => cb(Right()))
      def onMessage(cb: EitherOutput => Unit): Unit =
        ws.onmessage = wsEvent => cb(decode[shared.messages.Output](wsEvent.data.toString))
      val onCloseStream = Stream.eval(onCloseF)

      Queue.unbounded[F, EitherOutput].flatMap { inputQueue =>
        Dispatcher[F].use { dispatcher =>
          Async[F].delay { onMessage(msg => dispatcher.unsafeRunAndForget(inputQueue.offer(msg))) } >> {
            val inStream  = Stream.fromQueueUnterminated(inputQueue).through(inputPipe)
            val outStream = outputStream.evalMap(msg => secureSend(ws, msg)) ++ Stream.eval(Async[F].delay(ws.close()))
            Stream(outStream, inStream, onCloseStream).parJoinUnbounded.compile.drain
          }
        }
      }
    }
}
