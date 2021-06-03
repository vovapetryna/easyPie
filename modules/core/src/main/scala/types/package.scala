import eu.timepit.refined.api._
import eu.timepit.refined.collection.NonEmpty

package object types {
  type NeString = String Refined NonEmpty

  object NeString extends RefinedTypeOps[NeString, String]
}
