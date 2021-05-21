import eu.timepit.refined.api._
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric._
import eu.timepit.refined.string._

import scala.util.Try

package object types {
  type NeString = String Refined NonEmpty
  type Id       = Long Refined GreaterEqual[0]
  type Email    = String Refined MatchesRegex["""^\S+@\S+$"""]
  type Url      = String Refined Uri

  object NeString extends RefinedTypeOps[NeString, String]
  object Id       extends RefinedTypeOps[Id, Long]
  object Email    extends RefinedTypeOps[Email, String]
  object Url      extends RefinedTypeOps[Url, String]

  object PathId { def unapply(value: String): Option[Id] = Try(value.toLong).toOption.flatMap(Id.from(_).toOption) }
}
