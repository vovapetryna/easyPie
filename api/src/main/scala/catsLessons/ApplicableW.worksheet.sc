import cats._
import cats.data._
import cats.implicits._

// --- Semigroupal Options
// law product must be associative

Semigroupal.tuple2(1.pure[Option], "test".pure[Option])

case class User(age: Int, name: String)
Semigroupal.map2(1.pure[Option], "test".pure[Option])(User.apply)

(1.pure[Option], 2.pure[Option]).tupled

(10.pure[Option], "Vasya".pure[Option]).mapN(User.apply)

// --- Paralel

(List(Option(1), Option(2)), List(Option(1), Option(2))).parTupled