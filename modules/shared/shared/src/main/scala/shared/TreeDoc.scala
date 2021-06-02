package shared

import scala.scalajs.js.JSConverters._

@scala.scalajs.js.annotation.JSExportTopLevel("TreeDoc")
object TreeDoc {
  @scala.scalajs.js.annotation.JSExport("init")
  def init(siteId: Int): TreeDoc = new TreeDoc(
    Tree.branch(
      Atom(Disambiguate(0, siteId), "."),
      Tree.leaf(Atom(Disambiguate(1, siteId), "")) :: Nil,
      Tree.leaf(Atom(Disambiguate(2, siteId), "")) :: Nil
    ),
    3,
    siteId
  )
}

@scala.scalajs.js.annotation.JSExportAll
class TreeDoc(data: STree, localId: Int, siteId: Int) {
  def represent: List[SAtom]                = data.infixReduce(a => a :: Nil)
  def jsRepr: scala.scalajs.js.Array[SAtom] = represent.toJSArray

  def put(value: String, left: SAtom, right: SAtom): TreeDoc = new TreeDoc(
    data.put(Atom(Disambiguate(localId, siteId), value), left, right),
    localId + 1,
    siteId
  )
}
