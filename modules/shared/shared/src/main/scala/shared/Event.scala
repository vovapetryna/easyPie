package shared

sealed trait Event[T]
final case class AddLeft[T](value: T)                            extends Event[T]
final case class AddRight[T](value: T)                           extends Event[T]
final case class Add[T](value: T, left: Atom[T], right: Atom[T]) extends Event[T]
final case class Delete[T](atom: Atom[T])                        extends Event[T]
