package models

import cats.effect.std.Semaphore

case class Locks[F[_]](locks: Map[String, Semaphore[F]]) {
  def get(id: String): Option[Semaphore[F]]             = locks.get(id)
  def addLock(id: String, lock: Semaphore[F]): Locks[F] = copy(locks = locks + (id -> lock))
}

object Locks { def init[F[_]]: Locks[F] = Locks(Map.empty) }
