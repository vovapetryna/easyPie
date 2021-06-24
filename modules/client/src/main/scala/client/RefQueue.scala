package client

import cats.effect.std.Semaphore
import cats.effect.{Async, Ref}
import cats.implicits._
import fs2.Stream

class RefQueue[F[_]: Async, T](
    val values: Ref[F, Vector[Option[T]]],
    val size: Semaphore[F],
    val ackRef: Ref[F, Option[T]],
    val ackSize: Semaphore[F]
) {
  def offer(value: Option[T]): F[Unit] = for {
    _ <- values.getAndUpdate(_.+:(value))
    _ <- size.release
  } yield ()

  def take: F[Option[T]] = for {
    _      <- size.acquire
    _      <- ackSize.acquire
    result <- values.modify(v => v.dropRight(1) -> v.last)
    _      <- ackRef.getAndSet(result)
  } yield result

  def unlock: F[Unit] = ackRef.getAndSet(None) >> ackSize.release

  def read: F[Vector[T]] = for {
    sendQueueValues <- values.get
    ackQueueValues  <- ackRef.get
  } yield (sendQueueValues :+ ackQueueValues).collect { case Some(v) => v }

  def stream: Stream[F, T] = Stream.eval(take).flatMap {
    case None    => Stream.empty
    case Some(c) => Stream(c) ++ stream
  }
}

object RefQueue {
  def empty[F[_]: Async, T]: F[RefQueue[F, T]] = for {
    ref     <- Ref.of[F, Vector[Option[T]]](Vector.empty)
    state   <- Semaphore[F](0)
    ackRef  <- Ref.of[F, Option[T]](None)
    ackSize <- Semaphore[F](1)
  } yield new RefQueue[F, T](ref, state, ackRef, ackSize)
}
