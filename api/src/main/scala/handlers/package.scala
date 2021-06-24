import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import org.mongodb.scala.bson.ObjectId

package object handlers {
  type Handler[F[_], State] = State => PartialFunction[shared.messages.Input, F[Unit]]

  case class Input[F[_]](id: String, topicsQ: routes.TopicsQ[F], locksQ: routes.LocksQ[F], repo: mongos.Repos[F], session: models.Account.Session)

  def defaultHandler[F[_]: Async]: Handler[F, Input[F]] = _ => { m => Async[F].delay(println(s"unhandled_msg_[$m]")) }

  def reloadHandler[F[_]: Async]: Handler[F, Input[F]] = state => { case shared.messages.Input.Reload() =>
    val Input(id, topicsQ, _, repo, session) = state
    val reloadT = for {
      document <- OptionT(repo.otDocument.byIdNoOps(new ObjectId(id)))
      topics   <- OptionT.liftF(topicsQ.take)
      _        <- OptionT.liftF(topicsQ.offer(topics))
      _ <- OptionT(
        topics.topics
          .get(id)
          .map(_.publish1(shared.messages.Output.Reload(document.state, Set(session._id.toHexString), Set.empty)))
          .sequence
      )
    } yield ()
    reloadT.value.as()
  }

  def operationHandler[F[_]: Async]: Handler[F, Input[F]] = state => { case shared.messages.Input.Operation(operation, revision) =>
    val Input(id, topicsQ, locksQ, repo, session) = state
    val oId                                       = new ObjectId(id)
    val operationT = for {
      locks <- OptionT.liftF(locksQ.take)
      _     <- OptionT.liftF(locksQ.offer(locks))
      _     <- OptionT(locks.locks.get(id).map(_.acquire).sequence)

      document <- OptionT(repo.otDocument.byIdNoOps(oId))
      operation <-
        if (revision === document.revision + 1) {
          val newContent = shared.ot.executors.execute(document.content, operation)
          OptionT.liftF(repo.otDocument.update(document._id, newContent, document.revision + 1, operation)).as(operation)
        } else if (revision < (document.revision + 1) && revision > 0) {
          for {
            documentOps <- OptionT(repo.otDocument.byIdOps(oId, revision - 1, document.revision))
            resolvedOp = documentOps.operations.foldLeft(operation) { case (op, nextOp) => shared.ot.transformers.trans(op, nextOp) }
            newContent = shared.ot.executors.execute(documentOps.content, resolvedOp)
            _ <- OptionT.liftF(repo.otDocument.update(document._id, newContent, document.revision + 1, resolvedOp))
          } yield resolvedOp
        } else OptionT.none[F, shared.ot.Operation]
      topics <- OptionT.liftF(topicsQ.take)
      _      <- OptionT.liftF(topicsQ.offer(topics))
      _ <- OptionT(
        topics.topics
          .get(id)
          .map { t =>
            for {
              _ <- t.publish1(shared.messages.Output.Operation(operation, Set.empty, Set(session._id.toHexString)))
              _ <- t.publish1(shared.messages.Output.Ack(Set(session._id.toHexString), Set.empty))
            } yield ()
            }
          .sequence
      )

      _ <- OptionT(locks.locks.get(id).map(_.release).sequence)
    } yield ()

    operationT.value.as()
  }

  def handler[F[_]: Async](): Handler[F, Input[F]] = state =>
    reloadHandler.apply(state) orElse
      operationHandler.apply(state) orElse
      defaultHandler.apply(state)
}
