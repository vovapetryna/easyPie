package shared

import cats._
import cats.implicits._
import io.circe._
import io.circe.syntax._
import org.scalatest.flatspec.AnyFlatSpec

class TreeSpec extends AnyFlatSpec {
  val testTree: Tree[Int] = Tree.branch(
    1,
    Option(
      Tree.branch(
        2,
        Option(Tree.leaf(3)),
        Option(Tree.leaf(4))
      )
    ),
    Option(Tree.leaf(5))
  )

  "Tree Decode" should "parse single Leaf" in {
    val singleLeaf: String = """ {"value": 1 } """
    val parsed             = parser.decode[Tree[Int]](singleLeaf)
    assert(parsed === Right(Tree.leaf(1)))
  }
  it should "parse one level Branch" in {
    val singleLevelBranch: String = """ {"value": 1, "left": {"value": 2}, "right": {"value": 3} } """
    val parsed                    = parser.decode[Tree[Int]](singleLevelBranch)
    assert(parsed === Right(Tree.branch(1, Option(Tree.leaf(2)), Option(Tree.leaf(3)))))
  }
  it should "ignore left or right Branch" in {
    val ignoredBranchLeft: String  = """ {"value": 1, "right": {"value": 3} } """
    val parsedLeft                 = parser.decode[Tree[Int]](ignoredBranchLeft)
    val ignoredBranchRight: String = """ {"value": 1, "left": {"value": 2} } """
    val parsedRight                = parser.decode[Tree[Int]](ignoredBranchRight)
    assert(parsedLeft === Right(Tree.branch(1, None, Option(Tree.leaf(3)))))
    assert(parsedRight === Right(Tree.branch(1, Option(Tree.leaf(2)), None)))
  }

  "Tree Encode" should "serialize single Leaf" in {
    assert(Tree.leaf(1).asJson.noSpaces == """{"value":1,"left":null,"right":null}""")
  }
  it should "encode and decode large adr (check for StackOverflow)" in {
    @annotation.tailrec
    def treeBuildAux(base: Tree[Int], counter: Int): Tree[Int] = counter match {
      case 0 => base
      case _ => treeBuildAux(Tree.branch(counter, Option(base), Option(base)), counter - 1)
    }
    val tree = treeBuildAux(Tree.leaf(0), 5)
    assert(parser.decode[Tree[Int]](tree.asJson.noSpaces).isRight)
    assert(tree.asJson.noSpaces == parser.decode[Tree[Int]](tree.asJson.noSpaces).toOption.get.asJson.noSpaces)
  }

  "Tree Functor" should "preserv Tree structure" in {
    val testTree = Tree.branch(1, Option(Tree.leaf(2)), Option(Tree.leaf(3)))
    assert(Eq[Tree[Int]].eqv(testTree.map(_ + 1), Tree.branch(2, Option(Tree.leaf(3)), Option(Tree.leaf(4)))))
  }

  "Tree Foldable" should "fold left in infix order" in {
    val folded = testTree.foldLeft("")(_ + _.toString)
    assert(folded == "32415")
  }
  it should "fold right in reversed infix order" in {
    val folded = testTree.foldRight(Eval.later("")) { case (v, b) => b.map(_ + v.toString) }
    assert(folded.value == "51423")
  }

  "Tree flatMapL modifications" should "affect traverse path" in {
    testTree.flatMapL {
      case Tree(v, None, None) =>
        shared.Tree(v, Option(Tree.leaf(10)), Option(Tree.leaf(20)))
      case t => t
    }
  }
  it should "release put method" in {
    val putTestTree = Tree.leaf(Set.empty[String])
    val putRight    = putTestTree.put(Set("a"), Option(Set.empty[String]), None)
    val putLeft     = putRight.put(Set("b"), None, Option(Set.empty[String]))
    val putBetween1 = putLeft.put(Set("c"), Option(Set("b")), Option(Set.empty[String]))
    val conflictAdd = putBetween1.put(Set("d"), Option(Set("b")), Option(Set.empty[String]))

    println(putRight.foldLeft(Set.empty[String] :: Nil)(_ :+ _))
    println(putLeft.foldLeft(Set.empty[String] :: Nil)(_ :+ _))
    println(putBetween1.foldLeft(Set.empty[String] :: Nil)(_ :+ _))
    println(conflictAdd.foldLeft(Set.empty[String] :: Nil)(_ :+ _))
  }
}
