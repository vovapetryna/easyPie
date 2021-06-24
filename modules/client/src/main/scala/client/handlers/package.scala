package client
import cats.effect.{Async, Ref}
import cats.implicits._

package object handlers {
  type Handler[F[_], State] = State => PartialFunction[shared.messages.Output, F[Unit]]

  case class Input[F[_]](stateRef: Ref[F, shared.ot.State], sendQueue: RefQueue[F, shared.ot.Operation], updateState: shared.ot.State => Unit)

  def defaultHandler[F[_]: Async]: Handler[F, Input[F]] = _ => { case msg => Async[F].delay(println("unhandled_msg", msg)) }

  def ackHandler[F[_]: Async]: Handler[F, Input[F]] = state => { case _: shared.messages.Output.Ack =>
    state.sendQueue.unlock
  }

  def reloadHandler[F[_]: Async]: Handler[F, Input[F]] = state => { case shared.messages.Output.Reload(newState, _, _) =>
    Async[F].delay(state.updateState(newState)) >> state.stateRef.update(_ => newState)
  }

  def operationHandler[F[_]: Async]: Handler[F, Input[F]] = state => { case op: shared.messages.Output.Operation =>
    state.sendQueue.read
      .map(_.foldLeft(op.operation) { case (acc, n) => shared.ot.transformers.trans(acc, n) })
      .flatMap { operation =>
        state.stateRef.modify { old =>
          val newState = old.next(shared.ot.executors.execute(old.value, operation))
          newState -> newState
        }
      }
      .flatMap(newState => Async[F].delay(state.updateState(newState)))
  }

  def handler[F[_]: Async]: Handler[F, Input[F]] = state =>
    ackHandler.apply(state) orElse
      operationHandler.apply(state) orElse
      reloadHandler.apply(state) orElse
      defaultHandler.apply(state)

}
