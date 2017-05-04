package renderer

import exceptions.IllegalPortChoice
import globalvariables.VariableStorage
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.MouseEvent
import sharednodejsapis.{BrowserWindow, BrowserWindowOptions, Path}

import scala.scalajs.js

/**
 * Manage what happens in the create server html file.
 */
object CreateServer {
  if (scala.scalajs.LinkingInfo.developmentMode) println("We are in Create Server")

  def createServer(port: Int): Unit = {
    VariableStorage.storeValue("isThereServer", port.toString)

    VariableStorage.storeGlobalValue("serverPort", port.toString)

    val win = new BrowserWindow(new BrowserWindowOptions {
      override val width: js.UndefOr[Int] = 1200
      override val height: js.UndefOr[Int] = 600
    })
    win.loadURL("file://" +
      Path.join(js.Dynamic.global.selectDynamic("__dirname").asInstanceOf[String],
        "../../server/server/server.html")
    )
    win.webContents.openDevTools()

    if (!scala.scalajs.LinkingInfo.developmentMode) {
      win.hide()
    }

    dom.window.location.href = "../mainscreen/mainscreen.html"
  }

  dom.document.getElementById("confirm").asInstanceOf[html.Anchor].onclick = (_: MouseEvent) => {
    try {
      val portContent = dom.document.getElementById("inputPort").asInstanceOf[html.Input].valueAsNumber

      if (portContent < 1024 || portContent > 65535)
        throw IllegalPortChoice(s"Port number should be comprised between 1024 and 65535 (actual: $portContent)")

      dom.document.getElementById("portError").asInstanceOf[html.Div].style.visibility = "hidden"
      createServer(portContent)

    } catch {
      case illegalPort: IllegalPortChoice =>
        val portError = dom.document.getElementById("portError").asInstanceOf[html.Div]
        portError.style.visibility = "visible"
        portError.innerHTML = illegalPort.msg
      case _: Throwable =>
        val portError = dom.document.getElementById("portError").asInstanceOf[html.Div]
        portError.style.visibility = "visible"
        portError.innerHTML = "Malformed port number"
    }
  }

}
