import cats._
import cats.data._
import cats.implicits._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

// --- My composer (Transformer)

case class ComposedO[M1[_], A](value: M1[Option[A]]) {
  def flatMap[B](f: A => ComposedO[M1, B])(implicit m1: Monad[M1]) = flatMapF(f.map(_.value))
  def flatMapF[B](f: A => M1[Option[B]])(implicit m1: Monad[M1])   = ComposedO(m1.flatMap(value)(_.fold(Option.empty[B].pure[M1])(f)))
  def map[B](f: A => B)(implicit m1: Monad[M1])                    = flatMap(a => ComposedO(f(a).pure[Option].pure[M1]))
}

type ComposedListOption[A] = ComposedO[List, A]

class ComposedOMonadList extends Monad[ComposedListOption] {
  override def flatMap[A, B](fa: ComposedListOption[A])(f: A => ComposedListOption[B]): ComposedListOption[B] = fa.flatMap(f)
  override def pure[A](x: A): ComposedListOption[A]                                                           = ComposedO(x.pure[Option].pure[List])
  override def tailRecM[A, B](a: A)(f: A => ComposedListOption[Either[A, B]]): ComposedListOption[B]          = ???
  override def map[A, B](fa: ComposedListOption[A])(f: A => B): ComposedListOption[B]                         = fa.flatMap(f.map(r => pure(r)))
}

//todo:: find a way to declare type transformation M[_] ~> ComposedO[M[_], A]
// class ComposedOMonad[M[_], A] extends Monad[[X] =>> ComposedO[X, A]] {}

val testMonad = ComposedO(List(Option(1), Option(2)))
(for {
  a <- testMonad
  b <- testMonad
} yield (a + b) * 2).value

// --- Stack monads with transformers

type FutureEitherOption[A] = OptionT[[B] =>> EitherT[Future, String, B], A]

(for {
  a <- 100.pure[FutureEitherOption]
  b <- 20.pure[FutureEitherOption]
} yield a + b)
