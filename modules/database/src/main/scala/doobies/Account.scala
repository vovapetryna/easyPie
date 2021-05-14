package doobies

import cats.effect.Sync
import doobie._
import doobie.implicits._
import doobie.refined.implicits._
import types._

final class Account[F[_]: Sync](tx: Transactor[F]) extends repositories.Account[F] {
  override def getById(id: Id): F[Option[models.Account]] =
    sql"""SELECT * FROM accounts WHERE id = $id""".query[models.Account].option.transact(tx)

  override def insert(account: models.Account): F[Int] =
    sql"""INSERT INTO "accounts" ("name", "email", "password") VALUES (${account.name}, ${account.email}, ${account.password})""".update.run
      .transact(tx)

  override def getByEmailAndPassword(email: Email, password: NeString): F[Option[models.Account]] =
    sql""" SELECT * FROM accounts WHERE email = $email AND password = $password """.query[models.Account].option.transact(tx)
}
