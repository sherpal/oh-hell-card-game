package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


@js.native
@JSImport("fs", JSImport.Namespace)
object FileSystem extends js.Object {

  def readFile(path: String, callback: js.Function): Unit = js.native

  def writeFile(path: String, data: String, callback: js.Function): Unit = js.native

}
