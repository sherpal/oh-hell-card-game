package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|


@js.native
@JSImport("fs", JSImport.Namespace)
object FileSystem extends js.Object {

  def readFile(path: String, callback: js.Function): Unit = js.native

  def writeFile(path: String, data: String | Buffer, callback: js.Function): Unit = js.native

}
