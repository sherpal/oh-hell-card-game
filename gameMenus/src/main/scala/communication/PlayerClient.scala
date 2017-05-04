package communication

import networkcom._
import org.scalajs.dom
import org.scalajs.dom.html

/**
 * A PlayerClient can ask to join a game
 */
class PlayerClient(val name: String, val gameName: String, val address: String, val port: Int, registrationId: Int)
  extends Client with PlayerSocket {

  connect()

  def messageCallback(client: Client, msg: Message): Unit = {
    msg match {
      case GameLaunched(_, password, gameStateMessage) =>
        launchGameCallback(password, gameStateMessage)

      case LeaveGame(_, playerName) if playerName == name =>
        leaveGameCallback()

      case CancelGame(_) =>
        cancelGameCallback()

      case NbrCardsInHand(_, n) =>
        currentNbrCards = n
        dom.document.getElementById("nbrCards").asInstanceOf[html.Span].innerHTML =
          s"Maximum number of cards in hand : $n"

      case DeckMessage(_, _, _) =>

      case CurrentPlayers(_, names) =>
        updatePlayerList(names)

      case GameCreated(_, _) =>
        dom.console.warn(s"Received $msg but shouldn't have. I'll just ignore it.")

      case _ =>
        println(msg)
    }
  }

  def connectedCallback(client: Client, peer: Peer, connected: Boolean): Unit = {
    if (connected) {
      sendReliable(NewPlayer(gameName, name, registrationId))
    }
  }


}
