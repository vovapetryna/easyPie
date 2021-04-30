import cats.Functor
import cats.implicits._

// --- Covariant Functor

sealed trait Tree[+A]
case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
case class Leaf[A](value: A)                        extends Tree[A]

object Tree {
  def branch[A](left: Tree[A], right: Tree[A]): Tree[A] = Branch(left, right)
  def leaf[A](value: A): Tree[A]                        = Leaf(value)
}

implicit val treeFunctor: Functor[Tree] = new Functor[Tree] {
  override def map[A, B](fa: Tree[A])(f: A => B): Tree[B] = fa match {
    case Branch(left, right) => Branch(map(left)(f), map(right)(f))
    case Leaf(value)         => Leaf(f(value))
  }
}

val testTree = Tree.branch(Tree.branch(Tree.leaf(1), Tree.leaf(2)), Tree.leaf(3))

Functor[Tree].map(testTree)(_ - 1)
testTree.map(_ - 1)

// --- Contravariant Functor

trait Printable[A] {
  def format(value: A): String
  def contramap[B](f: B => A): Printable[B] = (value: B) => this.format(f(value))
}
implicit class PrintableSyntax[A](value: A)(implicit printable: Printable[A]) {
  def print: Unit = println(printable.format(value))
}

implicit val printableDouble: Printable[Double] = _.toString
implicit val printableInt: Printable[Int]       = printableDouble.contramap(_.toDouble)
10.print

case class Box[A](value: A)
implicit def printableBox[A](implicit pA: Printable[A]): Printable[Box[A]] = (value: Box[A]) => s"Box of ${pA.format(value.value)}"
Box(20).print

// --- Invariant Functor
