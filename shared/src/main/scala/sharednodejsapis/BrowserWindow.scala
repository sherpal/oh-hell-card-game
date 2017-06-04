package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, ScalaJSDefined}


@js.native
@JSImport("electron", "remote.BrowserWindow")
class BrowserWindow(options: BrowserWindowOptions) extends EventEmitter {


  val webContents: WebContents = js.native

  val id: Integer = js.native

  def close(): Unit = js.native

  def loadURL(url: String): Unit = js.native

  def show(): Unit = js.native

  def hide(): Unit = js.native

  def setMenu(menu: js.Object): Unit = js.native //TODO: change this when need of a menu

  def getParentWindow(): BrowserWindow = js.native

  def removeAllListeners(eventType: String): Unit = js.native

  def focus(): Unit = js.native
}


@js.native
@JSImport("electron", "remote.BrowserWindow")
object BrowserWindow extends EventEmitter {
  /** Returns Array of [[BrowserWindow]] - An array of all opened browser windows. */
  def getAllWindows(): js.Array[BrowserWindow] = js.native

  /** Returns [[BrowserWindow]] - The window that is focused in this application, otherwise returns null. */
  def getFocusedWindow(): BrowserWindow = js.native

  /** Returns [[BrowserWindow]] - The window that owns the given webContents. */
  def fromWebContents(webContents: WebContents): BrowserWindow = js.native

  /** Returns [[BrowserWindow]] - The window with the given id. */
  def fromId(id: Int): BrowserWindow = js.native

  def addDevToolsExtension(path: String): Unit = js.native

  def removeDevToolsExtension(name: String): Unit = js.native

}


@ScalaJSDefined
trait BrowserWindowOptions extends js.Object {
  /** Width. Default value: 800. */
  val width: js.UndefOr[Int] = js.undefined
  /** Height. Default value: 600. */
  val height: js.UndefOr[Int] = js.undefined

  /** Whether BrowserWindow will be visible. Default value: true. */
  val show: js.UndefOr[Boolean] = js.undefined

  /** Whether BrowserWindow will have encircling Frame. Default value: true. */
  val frame: js.UndefOr[Boolean] = js.undefined

  /** Parent of the BrowserWindow */
  val parent: js.UndefOr[BrowserWindow] = js.undefined

  /** Whether BrowserWindow is modal. Default value: false. */
  val modal: js.UndefOr[Boolean] = js.undefined

  /** Whether BrowserWindow is resizable. Default value: true. */
  val resizable: js.UndefOr[Boolean] = js.undefined
}

