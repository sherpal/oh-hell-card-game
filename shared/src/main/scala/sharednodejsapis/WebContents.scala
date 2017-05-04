package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal


@js.native
@JSGlobal
abstract class WebContents extends EventEmitter {

  /** Opens the DevTools. */
  def openDevTools(): Unit = js.native

  def send(channel: String, args: Any*): Unit = js.native

}
