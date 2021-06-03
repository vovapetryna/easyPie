package shared.tree

import cats.implicits._
import org.scalatest.flatspec.AnyFlatSpec

class TreeSpec extends AnyFlatSpec {

  val testTree: Tree[String] = Tree.branch(
    "1",
    Tree.branch(
      "2",
      Tree.leaf("3") :: Nil,
      Tree.leaf("4") :: Nil
    ) :: Nil,
    Tree.leaf("5") :: Nil
  )
  val initTree: Tree[String] = Tree.branch("", Tree.leaf("Left->") :: Nil, Tree.leaf("<-Right") :: Nil)

  "Tree infixReduce" should "fold left in infix order" in {
    val folded = testTree.infixReduce(identity)
    assert(folded == "32415")
  }

  "Put" should "add nodes" in {
    val operations = Seq[Tree[String] => Tree[String]](
      _.put("a", "", "<-Right"),
      _.put("b", "Left->", ""),
      _.put("c", "b", ""),
      _.put("C", "b", ""),
      _.put("e", "b", "C"),
      _.put("k", "C", "c"),
      _.put("K", "C", "c"),
      _.put("f", "c", "")
    )

    operations.foldLeft(initTree) { case (acc, opp) =>
      val next = opp(acc)
      println(next, next.infixReduce(identity))
      next
    }
  }

  "Find" should "find element and return path" in {
    println(testTree.find(_ == "6"))
  }
  it should "work with ancestor" in {
    val pathOfF = testTree.find(_ == "1")
    val pathOfS = testTree.find(_ == "3")

    assert(testTree.ancestor(pathOfF, pathOfS))
    assert(!testTree.ancestor(pathOfS, pathOfF))
  }
}
