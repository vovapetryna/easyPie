import cats.effect.Async
import cats.implicits._
import types.Handler

package object handlers {
  case class Input[F[_]](id: Long, topicsR: routes.TopicsR[F])

  def simpleHandler[F[_]: Async](): Handler[F, Input[F]] = state => { case models.InputMessages.Simple(value) =>
    state.topicsR.get.flatMap(topics => topics.topics.get(state.id).map(_.publish1(models.OutputMessages.Simple(value))).sequence.as(()))
  }

  def handler[F[_]: Async](): Handler[F, Input[F]] = state => simpleHandler().apply(state)
}
