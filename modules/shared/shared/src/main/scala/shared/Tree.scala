package shared

import cats._
import cats.derived._
import cats.implicits._
import io.circe._
import io.circe.generic.semiauto._

@scala.scalajs.js.annotation.JSExportAll
final case class Tree[T](value: T, left: List[Tree[T]] = Nil, right: List[Tree[T]] = Nil) {
  def put(value: T, leftValue: Option[T], rightValue: Option[T])(implicit e: Eq[T]): Tree[T] = flatMapL {
    case Tree(v, l, Nil) if leftValue.exists(_ === v)  => Tree(v, l, Tree.leaf(value) :: Nil)
    case Tree(v, Nil, r) if rightValue.exists(_ === v) => Tree(v, Tree.leaf(value) :: Nil, r)
    case Tree(v, l, r) if r.nonEmpty && leftValue.exists(_ === v) && rightValue.forall(t => r.forall(_.value =!= t)) =>
      Tree(v, l, Tree.leaf(value) :: r)
    case Tree(v, l, r) if l.nonEmpty && rightValue.exists(_ === v) && leftValue.forall(t => l.forall(_.value =!= t)) =>
      Tree(v, Tree.leaf(value) :: l, r)
    case t => t
  }

  def flatMapL(f: Tree[T] => Tree[T]): Tree[T] = this match {
    case t if t.left.isEmpty || t.right.isEmpty => f(t)
    case Tree(v, l, r)                          => Tree(v, l.map(_.flatMapL(f)), r.map(_.flatMapL(f)))
  }

  def foldLeft[B](b: B)(f: (B, T) => B)(implicit m: Monoid[B]): B = this match {
    case Tree(v, Nil, Nil) => f(b, v)
    case Tree(v, l, r) =>
      val left = l.map(_.foldLeft(b)(f)).reduceLeft(_ |+| _)
      val fold = f(left, v)
      r.map(_.foldLeft(fold)(f)).reduceLeft(_ |+| _)
  }
}

@scala.scalajs.js.annotation.JSExportTopLevel("TreeDoc")
object Tree {
  @scala.scalajs.js.annotation.JSExport("branch")
  def branch[T](value: T, left: List[Tree[T]], right: List[Tree[T]]): Tree[T] = Tree(value, left, right)
  @scala.scalajs.js.annotation.JSExport("leaf")
  def leaf[T](value: T): Tree[T] = Tree(value, Nil, Nil)

  implicit def r[T](implicit r: Decoder[T]): Decoder[Tree[T]] = deriveDecoder
  implicit def w[T](implicit w: Encoder[T]): Encoder[Tree[T]] = deriveEncoder
  implicit def eq[T](implicit e: Eq[T]): Eq[Tree[T]]          = semiauto.eq

  implicit def functor[T]: Functor[Tree] = new Functor[Tree] {
    def map[A, B](fa: Tree[A])(f: A => B): Tree[B] = Tree(f(fa.value), fa.left.map(l => map(l)(f)), fa.right.map(r => map(r)(f)))
  }
}
