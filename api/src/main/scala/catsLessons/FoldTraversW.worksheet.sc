import cats._
import cats.data._
import cats.implicits._

// --- Foldable

List(1, 2 , 3, 4).foldLeft(List.empty[Int])((a, i) => i :: a)
List(1, 2 , 3, 4).foldRight(List.empty[Int])((i, a) => i :: a)

def map[A, B](fa: List[A])(f: A => B): List[B] = fa.foldRight(List.empty[B])((n, a) => f(n) :: a)
def flatMap[A, B](fa: List[A])(f: A => List[B]) = fa.foldRight(List.empty[B])((n, a) => f(n) |+| a)
def filter[A](fa: List[A])(c: A => Boolean) = fa.foldRight(List.empty[A])((n, a) => if (c(n)) n :: a else a)

map(List(1, 2, 3, 4))(_ + 2)
flatMap(List(1, 2, 3, 4))(r => r :: r :: Nil)
filter(List(1, 2, 3, 4, 5, 6))(_ % 2 == 0)

// --- Traversable

