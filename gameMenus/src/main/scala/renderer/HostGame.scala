package renderer

import communication.PreGameClient
import globalvariables.VariableStorage
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.MouseEvent
import parsinginputs.RetrieveInfo

/**
 * Manage what happens in the host game html file.
 */
object HostGame {

  private val isThereServer = VariableStorage.retrieveValue("isThereServer")
  if (isThereServer != null) {
    dom.document.getElementById("inputPort").asInstanceOf[html.Input].value = isThereServer.toString
    dom.document.getElementById("inputAddress").asInstanceOf[html.Input].value = "localhost"
  }

  private var preGameClient: Option[PreGameClient] = None


  dom.document.getElementById("confirm").asInstanceOf[html.Anchor].onclick = (_: MouseEvent) => {
    val (playerName, gameName, address, port) = RetrieveInfo()
    if (playerName != "") {
      preGameClient match {
        case Some(client) =>
          if (client.waitingForAnswer) {
            println("client is still waiting for answer...")
          } else {
            preGameClient = Some(new PreGameClient(address, port, playerName, gameName, host = true))
          }
        case None =>
          preGameClient = Some(new PreGameClient(address, port, playerName, gameName, host = true))
      }

    }
  }
}

