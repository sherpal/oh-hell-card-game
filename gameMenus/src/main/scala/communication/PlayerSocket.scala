package communication

import globalvariables.VariableStorage._
import networkcom.{Client, GameStateMessage, LeaveGame}
import org.scalajs.dom
import org.scalajs.dom.{Element, html}
import org.scalajs.dom.raw.Event

import scala.scalajs.js.JSConverters._

import scala.scalajs.js
import scala.scalajs.js.timers.{SetTimeoutHandle, clearTimeout, setTimeout}

/**
 * A PlayerSocket is either the computer that hosts the game (the one who manages game settings) or a player joining a
 * game.
 */
trait PlayerSocket extends Client {
  val name: String

  val gameName: String

  protected var currentNbrCards: Int = 1

  private val cancelButton: html.Anchor = dom.document.getElementById("back").asInstanceOf[html.Anchor]

  cancelButton.onclick = (_: Event) => {
    sendReliable(LeaveGame(gameName, name))

    leaveGameHandle = Some(setTimeout(5000) {
      leaveGameCallback()
    })
  }

  private var leaveGameHandle: Option[SetTimeoutHandle] = None

  protected def cancelGameCallback(): Unit = {
    leaveGame(s"Game `$gameName` has been canceled.")
  }

  protected def leaveGameCallback(): Unit = {
    leaveGame(s"You have been disconnected from Game `$gameName`.")
  }

  private def leaveGame(msg: String): Unit = {
    if (leaveGameHandle.isDefined)
      clearTimeout(leaveGameHandle.get)
    leaveGameHandle = None

    disconnect()

    unStoreInfo()
    storeValue("gameCanceled", msg)

    dom.window.location.href = "../mainscreen/mainscreen.html"
  }

  protected var gameWasLaunched: Boolean = false

  protected def launchGameCallback(password: Int, gameStateMessage: GameStateMessage): Unit = {
    if (scala.scalajs.LinkingInfo.developmentMode)
      println("we should launch the game.")

    gameWasLaunched = true
    disconnect()

    storeValue("password", password)

    def toJSArray[A](array: Array[A]): js.Array[A] = array.toSeq.toJSArray

    storeValue("players", toJSArray(gameStateMessage.players.toArray))
    storeValue("maxNbrCards", gameStateMessage.maxNbrCards)

    unStoreValue("reservationId")
    unStoreValue("host")



    dom.window.location.href = "../../gameplaying/gameplaying/gameplayinginterface.html"
  }

  private def unStoreInfo(): Unit = {
    unStoreValue("gameName")
    unStoreValue("reservationId")
    unStoreValue("playerName")
    unStoreValue("address")
    unStoreValue("port")
    unStoreValue("host")
  }


  protected val playerListElement: html.UList = dom.document.getElementById("playerList").asInstanceOf[html.UList]
  protected def playerList: IndexedSeq[Element] = {
    val children = playerListElement.children
    (0 until children.length).map(children(_))
  }
  protected def nbrOfPlayers: Int = playerList.size
  protected def updatePlayerList(currentPlayers: Array[String]): Unit = {
    val childrenCollection = playerList.toSet
    childrenCollection
      .filterNot(elem => currentPlayers.contains(elem.innerHTML))
      .foreach(elem => elem.parentNode.removeChild(elem))

    currentPlayers.toSet
      .diff(childrenCollection.map(_.innerHTML))
      .foreach(playerName => {
        val li = dom.document.createElement("li").asInstanceOf[html.LI]
        playerListElement.appendChild(li)
        li.textContent = playerName
      })
  }

}
