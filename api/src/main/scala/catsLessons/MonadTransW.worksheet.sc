import cats._
import cats.data._
import cats.implicits._

// --- My composer (Transformer)

case class ComposedO[M1[_], A](value: M1[Option[A]]) {
  def flatMap[B](f: A => ComposedO[M1, B])(implicit m1: Monad[M1]) = flatMapF(f.map(_.value))
  def flatMapF[B](f: A => M1[Option[B]])(implicit m1: Monad[M1])   = ComposedO(m1.flatMap(value)(_.fold(Option.empty[B].pure[M1])(f)))
  def map[B](f: A => B)(implicit m1: Monad[M1]) = flatMap(a => ComposedO(f(a).pure[Option].pure[M1]))
} 

type ComposedListOption[A] = ComposedO[List, A]

class ComposedOMonadList extends Monad[ComposedListOption] {
  override def flatMap[A, B](fa: ComposedListOption[A])(f: A => ComposedListOption[B]): ComposedListOption[B] = fa.flatMap(f)
  override def pure[A](x: A): ComposedListOption[A] = ComposedO(x.pure[Option].pure[List])
  override def tailRecM[A, B](a: A)(f: A => ComposedListOption[Either[A, B]]): ComposedListOption[B] = ???
  override def map[A, B](fa: ComposedListOption[A])(f: A => B): ComposedListOption[B] = fa.flatMap(f.map(r => pure(r)))
}

val testMonad = ComposedO(List(Option(1), Option(2)))
(for {
  a <- testMonad
} yield a * 2).value
