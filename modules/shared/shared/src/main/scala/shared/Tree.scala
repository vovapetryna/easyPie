package shared

import cats._
import cats.derived._
import cats.implicits._
import io.circe._
import io.circe.generic.semiauto._

@scala.scalajs.js.annotation.JSExportAll
final case class Tree[T](value: T, left: List[Tree[T]] = Nil, right: List[Tree[T]] = Nil) {
//  def put(value: T, leftValue: Option[T], rightValue: Option[T])(implicit e: Eq[T]): Tree[T] = flatMapL(continue = true) {
//    case Tree(v, l, Nil) if leftValue.exists(_ === v)  => Tree(v, l, Tree.leaf(value) :: Nil) -> false
//    case Tree(v, Nil, r) if rightValue.exists(_ === v) => Tree(v, Tree.leaf(value) :: Nil, r) -> false
//    case Tree(v, l, r) if r.nonEmpty && leftValue.exists(_ === v) && rightValue.forall(t => r.forall(_.value =!= t)) =>
//      Tree(v, l, Tree.leaf(value) :: r) -> false
//    case Tree(v, l, r) if l.nonEmpty && rightValue.exists(_ === v) && leftValue.forall(t => l.forall(_.value =!= t)) =>
//      Tree(v, Tree.leaf(value) :: l, r) -> false
//    case t => t -> true
//  }._1

  def find(predicate: T => Boolean): List[T] = if (predicate(value)) value :: Nil
  else {
    for (t <- left ++ right) {
      val res = t.find(predicate)
      if (res.nonEmpty) return value :: res
    }
    Nil
  }

  def ancestor(a: List[T], c: List[T])(implicit e: Eq[T]): Boolean    = (a.size < c.size) && a.zip(c).forall { case (l, r) => l === r }
  def miniSibling(l: List[T], r: List[T])(implicit e: Eq[T]): Boolean = l.dropRight(1) === r.dropRight(1)

  def newPosId(leftPos: T, rightPos: T)(implicit e: Eq[T]): (List[T], Boolean) = {
    val lP = find(_ === leftPos)
    val rP = find(_ === rightPos)

    if (ancestor(lP, rP)) rP -> false
    else if (ancestor(rP, lP)) lP -> true
    else if (miniSibling(lP, rP)) lP -> true
    else ???
  }

  def insertToPodId(value: T, pos: (T, Boolean)): Tree[T] = this match {
    case Tree(v, l, r) if v == pos._1 => if (pos._2) Tree(v, l, Tree.leaf(value) :: r) else Tree(v, Tree.leaf(value) :: l, r)
    case Tree(v, l, r)                => Tree(v, l.map(_.insertToPodId(value, pos)), r.map(_.insertToPodId(value, pos)))
  }

  def put(value: T, leftValue: T, rightValue: T)(implicit e: Eq[T]): Tree[T] = {
    val (path, side) = newPosId(leftValue, rightValue)
    insertToPodId(value, (path.last, side))
  }

//  def flatMapL(continue: Boolean)(f: Tree[T] => (Tree[T], Boolean)): (Tree[T], Boolean) = (this, continue) match {
//    case r @ (_, false) => r
//    case (t, true) =>
//      val (modTree, usContinue) = f(t)
//      val left = modTree.left.foldLeft((List.empty[Tree[T]], usContinue)) { case ((trees, cont), n) =>
//        val (t, c) = n.flatMapL(cont)(f)
//        (trees :+ t, c)
//      }
//      val right = modTree.right.foldLeft((List.empty[Tree[T]], left._2)) { case ((trees, cont), n) =>
//        val (t, c) = n.flatMapL(cont)(f)
//        (trees :+ t, c)
//      }
//      modTree.copy(left = left._1, right = right._1) -> right._2
//  }

  def infixReduce(implicit m: Monoid[T], order: Order[T]): T = this match {
    case Tree(v, Nil, Nil) => v
    case Tree(v, l, r) =>
      l.map(_.infixReduce).foldLeft(Monoid[T].empty)(_ |+| _) |+| v |+| r.map(_.infixReduce).foldLeft(Monoid[T].empty)(_ |+| _)
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
