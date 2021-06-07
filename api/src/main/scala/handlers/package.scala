import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import org.mongodb.scala.bson.ObjectId

package object handlers {
  type Handler[F[_], State] = State => PartialFunction[models.InputMessages, F[Unit]]

  case class Input[F[_]](id: String, topicsR: routes.TopicsR[F], repo: mongos.Repos[F], session: models.Account.Session)

  def simpleHandler[F[_]: Async]: Handler[F, Input[F]] = state => { case models.InputMessages.Simple(value) =>
    state.topicsR.get.flatMap(topics =>
      topics.topics.get(state.id).map(_.publish1(models.OutputMessages.Simple(value, Set(state.session._id.toHexString), Set.empty))).sequence.as()
    )
  }

  def reloadHandler[F[_]: Async]: Handler[F, Input[F]] = state => { case models.InputMessages.Reload() =>
    val Input(id, topicsR, repo, session) = state
    val reloadT = for {
      document <- OptionT(repo.document.byId(new ObjectId(id)))
      topics   <- OptionT.liftF(topicsR.get.map(_.topics))
      _        <- OptionT(topics.get(id).map(_.publish1(models.OutputMessages.Reload(document, Set(session._id.toHexString), Set.empty))).sequence)
    } yield ()
    reloadT.value.as()
  }

  def actionHandler[F[_]: Async]: Handler[F, Input[F]] = state => { case models.InputMessages.Action(action) =>
    val Input(id, topicsR, repo, session) = state
    val oId                               = new ObjectId(id)
    val actionT = for {
      document <- OptionT(repo.document.byId(oId))
      newDocument = document.process(action)
      _      <- OptionT(repo.document.update(oId, newDocument).attempt.map(_.toOption))
      topics <- OptionT.liftF(topicsR.get.map(_.topics))
      _      <- OptionT(topics.get(id).map(_.publish1(models.OutputMessages.Action(action, Set.empty, Set(session._id.toHexString)))).sequence)
    } yield ()
    actionT.value.as()
  }

  def handler[F[_]: Async](): Handler[F, Input[F]] = state =>
    simpleHandler.apply(state) orElse
      reloadHandler.apply(state) orElse
      actionHandler.apply(state)
}
