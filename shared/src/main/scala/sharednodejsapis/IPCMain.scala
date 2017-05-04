package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, ScalaJSDefined}


@js.native
@JSImport("electron", "ipcMain")
object IPCMain extends EventEmitter {

}


@ScalaJSDefined
trait IPCMainEvent extends js.Object {
  val sender: WebContents

  var returnValue: Any
}
