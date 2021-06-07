import cats.effect.Async
import cats.implicits._
import mongo4cats.circe._
import mongo4cats.database.MongoDatabaseF

package object mongos {
  case class Repos[F[_]: Async](
      account: repositories.Account[F],
      document: repositories.Document[F],
      permission: repositories.Permission[F]
  )

  def init[F[_]: Async](db: MongoDatabaseF[F]): F[Repos[F]] = for {
    accountCol    <- db.getCollectionWithCirceCodecs[models.Account]("accounts")
    documentCol   <- db.getCollectionWithCirceCodecs[shared.tree.TreeDoc]("documents")
    permissionCol <- db.getCollectionWithCirceCodecs[models.Permission]("permissions")
  } yield Repos(
    new Account[F](accountCol),
    new Document[F](documentCol),
    new Permission[F](permissionCol)
  )
}
