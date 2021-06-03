package shared.tree

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

  def apply(data: STree, localId: Int)(implicit siteId: Int): TreeDoc = new TreeDoc(data, localId, siteId)
}

@scala.scalajs.js.annotation.JSExportAll
class TreeDoc(data: STree, localId: Int, implicit val siteId: Int) {
  def represent: List[SAtom]                = data.infixReduce(a => a :: Nil)
  def jsRepr: scala.scalajs.js.Array[SAtom] = represent.toJSArray

  def process(action: actions.Action): TreeDoc = {
    val newTree = action match {
      case actions.Action.Add(v, l, r, id) => data.put(Atom.create(localId, v)(id), l, r)
    }
    TreeDoc(newTree, localId + 1)
  }
}
