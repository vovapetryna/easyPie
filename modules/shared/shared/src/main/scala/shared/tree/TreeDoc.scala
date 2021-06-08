package shared.tree

import io.circe._
import io.circe.generic.JsonCodec
import shared.actions

import scala.scalajs.js.JSConverters._

@scala.scalajs.js.annotation.JSExportTopLevel("TreeDoc")
object TreeDoc {
  @scala.scalajs.js.annotation.JSExport("init")
  def init(implicit siteId: Int): TreeDoc = new TreeDoc(
    Tree.branch(
      Atom.create(0, "."),
      Tree.leaf(Atom.create(1, "")) :: Nil,
      Tree.leaf(Atom.create(2, "")) :: Nil
    ),
    3,
    siteId
  )
  @scala.scalajs.js.annotation.JSExport("loads")
  def loads(stringTree: String, siteId: Int): TreeDoc =
    parser.parse(stringTree).flatMap(_.as[TreeDoc]).toOption.map(_.copy(siteId = siteId)).getOrElse(init(siteId))
}

@scala.scalajs.js.annotation.JSExportAll
@JsonCodec case class TreeDoc(data: STree, localId: Int, implicit val siteId: Int) {
  def represent: List[SAtom]                = data.infixReduce(a => a :: Nil)
  def jsRepr: scala.scalajs.js.Array[SAtom] = represent.toJSArray

  def process(action: actions.Action): TreeDoc = {
    val newTree = action match {
      case actions.Action.Add(v, l, r, id) => data.put(Atom.create(localId, v)(id), l, r)
      case _                               => data
    }
    TreeDoc(newTree, localId + 1, siteId)
  }

  def addAction(value: String, left: SAtom, right: SAtom): actions.Action = actions.Action.Add(value, left, right, siteId)
  def processString(action: String): TreeDoc                              = process(parser.parse(action).flatMap(_.as[actions.Action]).getOrElse(actions.Action.nothing))

  lazy val rawTree: STree = data
}
