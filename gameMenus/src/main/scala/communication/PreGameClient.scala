package communication

import exceptions.DoesNotManageThisMessage
import globalvariables.VariableStorage
import networkcom._
import org.scalajs.dom

import scala.scalajs.js.timers.{clearTimeout, setTimeout}

/**
 * The PreGameClient will communicate to the server before creating or joining a game.
 *
 * It can be used to check whether
 * - a game name exists (good for joining, bad for creating)
 * - a game name has been booked (na for joining, bad for creating)
 * - a player name exists in some game (bad for joining, na for creating)
 *
 * @param address address of the server (can be "localhost")
 * @param port    port the server is listening to
 */
class PreGameClient(val address: String, val port: Int, playerName: String, gameName: String, host: Boolean)
  extends Client {


  private var _waitingAnswer: Boolean = true
  def waitingForAnswer: Boolean = _waitingAnswer

  connect()

  private val connectionHandle = setTimeout(5000) {
    println("did not manage to connect")
    endConnection()
    dom.window.alert("Did not manage to connect to server.")
  }

  private def endConnection(): Unit = {
    _waitingAnswer = false
    disconnect()
    clearTimeout(connectionHandle)
  }

  private def storeInfo(gName: String, reservationId: Int): Unit = {
    VariableStorage.storeValue("gameName", gName)
    VariableStorage.storeValue("reservationId", reservationId)
    VariableStorage.storeValue("playerName", playerName)
    VariableStorage.storeValue("address", address)
    VariableStorage.storeValue("port", port)
    VariableStorage.storeValue("host", if (host) 1 else 2)
  }


  def connectedCallback(client: Client, peer: Peer, status: Boolean): Unit = {
    if (status) {
      println("connected")
      if (host) {
        sendReliable(ReserveGameName(gameName))
      } else {
        sendReliable(ReservePlayerName(gameName, playerName))
      }
    } else if (waitingForAnswer) {
      endConnection()
    }
  }


  def messageCallback(client: Client, msg: Message): Unit = {
    try {
      if (scala.scalajs.LinkingInfo.developmentMode) println(s"received $msg")

      msg match {
        case GameNameReserved(name, reservationId, success) =>
          if (success) {
            if (scala.scalajs.LinkingInfo.developmentMode) println("should proceed to next web page")
            storeInfo(name, reservationId)
            dom.window.location.href = "hostgamesettings.html"
          } else {
            if (scala.scalajs.LinkingInfo.developmentMode) println("should print error message")
            dom.window.alert(s"Game Name `$name` is already used.")
          }
          endConnection()

        case PlayerNameReserved(gName, pName, reservationId, success) =>
          if (success) {
            if (scala.scalajs.LinkingInfo.developmentMode) println("should proceed to next web page")
            storeInfo(gName, reservationId)
            dom.window.location.href = "joingamesettings.html"
          } else {
            if (scala.scalajs.LinkingInfo.developmentMode) println("should print error message")
            dom.window.alert(s"Player Name `$pName` is already used.")
          }
          endConnection()

        case GameDoesNotExist(gName) =>
          if (scala.scalajs.LinkingInfo.developmentMode) println(s"game $gName does not exist.")
          dom.window.alert(s"Game `$gName` does not exist.")

        case TestMessage(message) =>
          if (scala.scalajs.LinkingInfo.developmentMode) println(message)

        case _ =>
          throw DoesNotManageThisMessage(s"Message type was ${msg.getClass}")
      }
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        dom.window.alert("Fatal error, try again to connect.")
    }
  }
}
