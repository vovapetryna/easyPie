import cats._
import cats.data._
import cats.implicits._

// --- My composer (Transformer)

case class ComposedO[M1[_], A](value: M1[Option[A]]) {
  def flatMap[B](f: A => ComposedO[M1, B])(implicit m1: Monad[M1]) = flatMapF(f.map(_.value))
  def flatMapF[B](f: A => M1[Option[B]])(implicit m1: Monad[M1])   = ComposedO(m1.flatMap(value)(_.fold(Option.empty[B].pure[M1])(f)))
} 

class ComposedOMonad[M1[_]: Monad, A] extends Monad[({type λ[α] = ComposedO[M1[α],A]})#λ] {
  val m1 = implicitly[Monad[M1]]
  override def flatMap[A, B](fa: ComposedO[M1, A])(f: A => ComposedO[M1, B]): ComposedO[M1, B] = fa.flatMap(f)
  override def pure[A](x: A): ComposedO[M1, A] = ComposedO(x.pure[Option].pure[M1])
  override def tailRecM[A, B](a: A)(f: A => ComposedO[M1, Either[A, B]]): ComposedO[M1, B] =
    ComposedO(
      m1.tailRecM(a)(a0 =>
        m1.map(f(a0).value)(
          _.fold[Either[A, Option[B]]](Right(None))(_.map(b => Some(b): Option[B]))
        )
      )
    )
}

val testMonad = List(Option(1), Option(2))
type ListOption[A] = List[Option[A]]
