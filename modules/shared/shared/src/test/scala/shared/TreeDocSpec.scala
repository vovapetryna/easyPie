package shared

import cats.implicits._
import org.scalatest.flatspec.AnyFlatSpec

class TreeDocSpec extends AnyFlatSpec {
  "Tree Doc" should "init empty tree" in {
    val empty = TreeDoc.init(1)
  }
  it should "add value to Tree Doc" in {
    val empty = TreeDoc.init(1)
    val left :: top :: right :: Nil = empty.represent

    val operations = Seq[TreeDoc => TreeDoc](
      _.put("a", top, right),
      _.put("b", left, top),
    )

    operations.foldLeft(empty) { case (acc, opp) =>
      val next = opp(acc)
      println(next.represent)
      next
    }
  }
}

