package client

import cats.effect.{Async, Ref}
import cats.implicits._
import fs2.{Pipe, Stream}

class Client[F[_]: Async](
    val stateRef: Ref[F, shared.ot.State],
    val sendQueue: RefQueue[F, shared.ot.Operation],
    val updateState: shared.ot.State => Unit
) {
  def nextText(newText: String, sid: Int): F[Unit] = stateRef
    .modify { state =>
      utils.text.changeOperation(state.value, newText, sid) match {
        case op if op.isId => state -> None
        case op =>
          val newState = state.next(shared.ot.executors.execute(state.value, op))
          updateState(newState)
          newState -> Option(op)
      }
    }
    .flatMap(op => if (op.nonEmpty) sendQueue.offer(op) else Async[F].delay())

  val outputStream: Stream[F, shared.messages.Input] =
    Stream.eval(Async[F].delay(shared.messages.Input.Reload())) ++
      sendQueue.stream.evalMap { operation =>
        stateRef.get.map(state => shared.messages.Input.Operation(operation, state.revision))
      }

  val inputPipe: Pipe[F, socket.EitherOutput, Unit] = inS =>
    inS
      .through(socket.errorHandlingPipe(msg => Async[F].delay(println(msg))))
      .evalMap(msg => handlers.handler.apply(handlers.Input(stateRef, sendQueue, updateState))(msg))

  def close: F[Unit] = sendQueue.offer(None)
}

object Client {
  def create[F[_]: Async](updateState: shared.ot.State => Unit): F[Client[F]] = {
    for {
      sendQueue <- RefQueue.empty[F, shared.ot.Operation]
      stateRef  <- Ref.of[F, shared.ot.State](shared.ot.State.empty)
    } yield new Client[F](
      stateRef,
      sendQueue,
      updateState
    )
  }
}
