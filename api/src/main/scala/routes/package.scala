import cats.effect.Ref

package object routes {
  type TopicsR[F[_]] = Ref[F, models.Topics[F]]
}
