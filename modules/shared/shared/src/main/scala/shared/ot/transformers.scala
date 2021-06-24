package shared.ot

import cats.implicits._

object transformers {
  type Transformer = (Operation, Operation) => Operation

  val insertInsert: Transformer = (o1, o2) =>
    if (o1.pos < o2.pos) o1
    else if (o1.pos === o2.pos && o1.sid < o2.sid) o1
    else o1.copy(pos = o1.pos + 1)

  val insertDelete: Transformer = (o1, o2) =>
    if (o1.pos <= o2.pos) o1
    else o1.copy(pos = o1.pos - 1)

  val deleteInsert: Transformer = (o1, o2) =>
    if (o1.pos < o2.pos) o1
    else o1.copy(pos = o1.pos + 1)

  val deleteDelete: Transformer = (o1, o2) =>
    if (o1.pos < o2.pos) o1
    else if (o1.pos > o2.pos) o1.copy(pos = o1.pos - 1)
    else Operation.id

  val trans: Transformer = (o1, o2) =>
    (o1, o2) match {
      case (o1, o2) if o1.isInsert && o2.isInsert => insertInsert(o1, o2)
      case (o1, o2) if o1.isInsert && o2.isDelete => insertDelete(o1, o2)
      case (o1, o2) if o1.isDelete && o2.isInsert => deleteInsert(o1, o2)
      case (o1, o2) if o1.isDelete && o2.isDelete => deleteDelete(o1, o2)
      case (o1, o2) if o1.isId && o2.isId         => o1
      case (o1, o2) if o1.isId && o2.isInsert     => o1
      case (o1, o2) if o1.isId && o2.isDelete     => o1
      case (o1, o2) if o1.isInsert && o2.isId     => o1
      case (o1, o2) if o1.isDelete && o2.isId     => o1
    }
}
