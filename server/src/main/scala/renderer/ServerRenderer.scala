package renderer

import gameserver.GameServer
import globalvariables.VariableStorage

import scala.scalajs.js.JSApp


object ServerRenderer extends JSApp {
  def main(): Unit = {

    val port = VariableStorage.retrieveGlobalValue("serverPort").asInstanceOf[String].toInt

    val server = new GameServer("*", port)
    server.activate()

  }
}
