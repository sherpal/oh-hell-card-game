package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, JSImport, ScalaJSDefined}

/**
 * OS module of Node.js.
 */
@js.native
@JSImport("os", JSImport.Namespace)
object OSModule extends js.Object {

  def networkInterfaces(): NetworkInterfaces = js.native

}



@ScalaJSDefined
trait NetworkInterface extends js.Object {
  val address: String
}

@ScalaJSDefined
trait NetworkInterfaces extends js.Object {
  val lo: js.Array[NetworkInterface]

  val eth0: js.Array[NetworkInterface]
}