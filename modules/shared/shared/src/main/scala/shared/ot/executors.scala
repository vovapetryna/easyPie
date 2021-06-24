package shared.ot

object executors {
  type Executor = (String, Operation) => String

  val insert: Executor = (value, o) => value.patch(o.pos, o.value, 0)
  val delete: Executor = (value, o) => value.patch(o.pos, "", 1)

  val execute: Executor = (value, o) =>
    (value, o) match {
      case (value, o) if o.isInsert => insert(value, o)
      case (value, o) if o.isDelete => delete(value, o)
      case (value, o) if o.isId     => value
    }
}
