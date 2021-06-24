package client.utils

import cats.implicits._

import scala.annotation.tailrec

object text {
  @tailrec
  def changeOperationAux(oldText: List[Char], newText: List[Char], sid: Int, pos: Int = 0): shared.ot.Operation =
    (oldText, newText) match {
      case (Nil, n :: Nil)                                                          => shared.ot.Operation.insert(pos, sid, n.toString)
      case (o :: Nil, Nil)                                                          => shared.ot.Operation.delete(pos, sid, o.toString)
      case (o :: tailO, n :: tailN) if o != n && tailO.length == (tailN.length + 1) => shared.ot.Operation.delete(pos, sid, o.toString)
      case (o :: tailO, n :: tailN) if o != n && tailO.length == (tailN.length - 1) => shared.ot.Operation.insert(pos, sid, n.toString)
      case (o :: _, n :: _) if o != n                                               => shared.ot.Operation.id
      case (_ :: tailO, _ :: tailN)                                                 => changeOperationAux(tailO, tailN, sid, pos + 1)
      case (_, _)                                                                   => shared.ot.Operation.id
    }

  def changeOperation(oldText: String, newText: String, sid: Int): shared.ot.Operation =
    changeOperationAux(oldText.toList, newText.toList, sid)
}
