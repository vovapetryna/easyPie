package doobies

import cats.effect._
import doobie._

case class Repos[F[_]: Sync](account: Account[F])

def repos[F[_]: Sync](tx: Transactor[F]): Repos[F] = Repos(
  new Account(tx)
)
