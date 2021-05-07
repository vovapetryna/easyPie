import cats._
import cats.implicits._
import cats.effect._
import cats.effect.concurrent.{Ref, Semaphore}
import collection.immutable.Queue

import scala.concurrent.ExecutionContext
def producer[F[_]: Sync: ContextShift](queueR: Ref[F, Queue[Long]], counterR: Ref[F, Long], filled: Semaphore[F], empty: Semaphore[F]): F[Unit] =
  Sync[F].uncancelable(for {
    _    <- empty.acquire
    i    <- counterR.getAndUpdate(_ + 1)
    prev <- queueR.getAndUpdate(_.enqueue(i + 1))
    _    <- filled.release
    _    <- if (i % 10000 == 0) Sync[F].delay(println(s"enqued $i messages")) else Sync[F].unit
    _    <- ContextShift[F].shift
  } yield ()) >> producer(queueR, counterR, filled, empty)

def consumer[F[_]: Sync: ContextShift](queueR: Ref[F, Queue[Long]], filled: Semaphore[F], empty: Semaphore[F]): F[Unit] =
  Sync[F].uncancelable(for {
    _        <- filled.acquire
    consumed <- queueR.modify(_.dequeue.swap)
    _        <- empty.release
    _        <- if (consumed % 10000 == 0) Sync[F].delay(println(s"totaly consumed $consumed")) else Sync[F].unit
    _        <- ContextShift[F].shift
  } yield ()) >> consumer(queueR, filled, empty)

{
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val program = for {
    queueR   <- Ref.of[IO, Queue[Long]](Queue.empty[Long])
    counterR <- Ref.of[IO, Long](0)
    filled   <- Semaphore[IO](0)
    empty    <- Semaphore[IO](100)
    consumers = List.range(1, 2).map(_ => consumer(queueR, filled, empty))
    producers = List.range(1, 2).map(_ => producer(queueR, counterR, filled, empty))
    res <- (consumers ++ producers).parSequence
      .as(ExitCode.Success)
      .handleErrorWith(ex => IO(println(s"error - ${ex.getMessage}")).as(ExitCode.Error))
  } yield res
}
