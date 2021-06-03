import eu.timepit.refined.api._
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric._

package object types {
  type NeString = String Refined NonEmpty
  type Id       = Long Refined GreaterEqual[0]

  object NeString extends RefinedTypeOps[NeString, String]
  object Id       extends RefinedTypeOps[Id, Long]
}
