import cats._
import cats.data._
import cats.implicits._
import scala.util.Try

trait MyMonad[F[_]] {
  def pure[A](a: A): F[A]
  def flatMap[A, B](value: F[A])(func: A => F[B]): F[B]
  def map[A, B](value: F[A])(func: A => B): F[B] = flatMap(value)(v => pure(func(v)))
}

// --- Id Monad

type Id[A] = A

implicit val monadId: Monad[Id] = new Monad[Id] {
  override def pure[A](x: A): Id[A]                                  = x
  override def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B]        = f(fa)
  override def tailRecM[A, B](a: A)(f: A => Id[Either[A, B]]): Id[B] = ???
}

// --- Either Monad

"error".asLeft[Int]
"1".asRight[String]

Either.catchNonFatal("-1".toInt)
-1.asRight[String].ensure("non_positive")(_ > 0)
"error".asLeft[Int].leftMap(_.toUpperCase)
"error".asLeft[Int].bimap(_.toUpperCase, _ * 100)

// --- MonadError
type EitherTh[A] = Either[Throwable, A]
implicit val monadError = MonadError[EitherTh, Throwable]
def validateAdult[F[_]](age: Int)(implicit me: MonadError[F, Throwable]): F[Int] =
  if (age >= 18) age.pure[F]
  else {
    (new IllegalArgumentException("Age must be greater than or equal to 18")).raiseError[F, Int]
  }
validateAdult[Try](16)
validateAdult[Try](18)

// --- Eval Monad

val now    = Eval.now(100)
val always = Eval.always(101 * math.random())
val later  = Eval.later(102 * math.random())

(now.value, now.value)
(always.value, always.value)
(later.value, later.value)

val combinedEval = for {
  a  <- now
  b  <- Eval.now { println("now B"); 100 }
  ca <- always
  c  <- Eval.always { println("always C"); ca }
} yield c

combinedEval.value

def factorial(value: Long): Eval[BigInt] =
  if (value == 1) Eval.now(value) else Eval.defer(factorial(value - 1).map(_ * value))
factorial(50000).value

def foldRight[A, B](as: List[A], acc: B)(fn: (A, B) => B): Eval[B] = as match {
  case head :: tail =>
    Eval.defer(foldRight(tail, acc)(fn).map(r => fn(head, r)))
  case Nil =>
    Eval.now(acc)
}

// --- Writer Monad

val writerLog = Vector("first", "second").tell
type LogT[A] = Writer[Vector[String], A]
val writerValue = 100.pure[LogT]
writerValue.value
writerLog.written

val writerFor = for {
  a <- 10.pure[LogT]
  b <- Vector("1", "2", "3", "4").tell
  c <- 20.writer(Vector("a", "b", "c"))
} yield a + c
writerFor.value
writerFor.written
writerFor.reset

// --- Reader Monad

case class Db(
    usernames: Map[Int, String],
    passwords: Map[String, String]
)
type DbReader[A] = Reader[Db, A]
def findUsername(userId: Int): DbReader[Option[String]]                  = Reader(_.usernames.get(userId))
def checkPassword(username: String, password: String): DbReader[Boolean] = Reader(_.passwords.get(username).exists(_ === password))
def checkLogin(userId: Int, password: String): DbReader[Boolean] = (for {
  user  <- OptionT(findUsername(userId))
  check <- OptionT.liftF(checkPassword(user, password))
} yield check).value.map(_.exists(_ === true))

val db = Db(Map(1 -> "vova"), Map("vova" -> "12345"))
findUsername(1).run(db)
checkPassword("vova", "12345").run(db)
checkLogin(1, "12345").run(db)

// --- State Monad

val a = State[Int, String](s => s -> s"state is $s")
a.run(100).value
val incS = State[Int, Int](s => (s + 1, s))
val program = for {
  _ <- incS
  _ <- incS
  _ <- incS
  r <- State.modify[Int](_ * 2)
} yield r
program.runA(0).value
program.runS(0).value

type CalcState[A] = State[List[Int], A]

def operand(num: Int): CalcState[Int] = State[List[Int], Int](s => (num :: s, num))
def operator(f: (Int, Int) => Int) = State[List[Int], Int] {
  case l :: r :: tail =>
    val ans = f(l, r)
    (ans :: tail, ans)
  case _ =>
    sys.error("omg")
}
def evalOne(s: String): CalcState[Int] =
  s match {
    case "+" => operator(_ + _)
    case "-" => operator(_ - _)
    case "/" => operator(_ / _)
    case "*" => operator(_ * _)
    case num => operand(Try(num.toInt).toOption.get)
  }
def evalAll(input: List[String]): CalcState[Int] = input.foldLeft(0.pure[CalcState]) { case (acc, n) => acc.flatMap(_ => evalOne(n)) }

evalOne("42").runA(Nil).value

val calc = for {
  _   <- evalOne("42")
  _   <- evalOne("32")
  ans <- evalOne("*")
} yield ans

calc.runA(Nil).value

evalAll("42" :: "10" :: "+" :: "10" :: "*" :: Nil).runA(Nil).value

// --- My custom Tree monad

sealed trait Tree[+A]
case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
case class Leaf[A](value: A)                        extends Tree[A]
def branch[A](left: Tree[A], right: Tree[A]): Tree[A] =
  Branch(left, right)
def leaf[A](value: A): Tree[A] =
  Leaf(value)

implicit def monadTree = new Monad[Tree] {
  override def flatMap[A, B](fa: Tree[A])(f: A => Tree[B]): Tree[B] = fa match {
    case Branch(l, r) => Branch(flatMap(l)(f), flatMap(r)(f))
    case Leaf(value)  => f(value)
  }
  override def pure[A](x: A): Tree[A] = Leaf(x)
  override def tailRecM[A, B](a: A)(f: A => Tree[Either[A, B]]): Tree[B] = flatMap(f(a)) {
    case Right(value) => Leaf(value)
    case Left(a)      => tailRecM(a)(f)
  }
}


