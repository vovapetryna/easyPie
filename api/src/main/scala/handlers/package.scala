import cats.effect.Async
import cats.implicits._

package object handlers {
  type Handler[F[_], State] = State => PartialFunction[models.InputMessages, F[Unit]]

  case class Input[F[_]](id: String, topicsR: routes.TopicsR[F])

  def simpleHandler[F[_]: Async](): Handler[F, Input[F]] = state => { case models.InputMessages.Simple(value) =>
    state.topicsR.get.flatMap(topics => topics.topics.get(state.id).map(_.publish1(models.OutputMessages.Simple(value))).sequence.as(()))
  }

  def handler[F[_]: Async](): Handler[F, Input[F]] = state => simpleHandler().apply(state)
}
