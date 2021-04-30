package repositories

import types._

trait Account[F[_]] {
  def getById(id: Id): F[Option[models.Account]]
  def insert(account: models.Account): F[Int]
}
