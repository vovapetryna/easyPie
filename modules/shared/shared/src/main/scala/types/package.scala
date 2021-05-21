import cats.Order
import cats.implicits._
import eu.timepit.refined.api._
import eu.timepit.refined.numeric._

package object types {
  type Id = Long Refined GreaterEqual[0]
  object Id extends RefinedTypeOps[Id, Long]
  implicit val idOrder: Order[Id] = Order.by(_.value)
}
