package communication

import networkcom._
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.{Event, KeyboardEvent}


class Host(val name: String, val gameName: String, val address: String, val port: Int, registrationId: Int)
  extends Client with PlayerSocket {

  connect()

  def sendDeck(cardsPerColor: Int, colors: Int): Unit = sendReliable(DeckMessage(gameName, cardsPerColor, colors))

  /**
   * Deck composition: (number of cards per color, number of color) (default to (13, 4), the usual deck)
   *
   * We will allow to change that in the future, but let's stick to that for now.
   */
  private val currentDeckComposition: (Int, Int) = (13, 4)
  def nbrOfCards: Int = currentDeckComposition._1 * currentDeckComposition._2


  dom.document.getElementById("inputNbrCards").asInstanceOf[html.Input].onkeyup = (_: KeyboardEvent) => sendNbrCards()
  dom.document.getElementById("inputNbrCards").asInstanceOf[html.Input].onchange = (_: Event) => sendNbrCards()
  def sendNbrCards(): Unit = {
    val n = try {
      dom.document.getElementById("inputNbrCards").asInstanceOf[html.Input].valueAsNumber
    } catch {
      case _: Throwable =>
        -1
    }
    if (n != currentNbrCards && n != -1) // sending only if need to update
      sendReliable(NbrCardsInHand(gameName, n))
  }
  private def validNbrCards: Boolean = nbrOfCards > 1 && nbrOfPlayers * currentNbrCards < nbrOfCards
  private def updateNbrCardsWarning(): Unit = {
    dom.document.getElementById("nbrCardsWarning").asInstanceOf[html.Span].style.visibility =
      if (validNbrCards) "hidden" else "visible"
  }


  private val launchButton: html.Anchor = dom.document.getElementById("confirm").asInstanceOf[html.Anchor]

  launchButton.onclick = (_: Event) => {
    if (validNbrCards && nbrOfPlayers > 1) {
      sendReliable(LaunchGame(gameName))
    } else if (nbrOfPlayers == 1) {
      dom.window.alert("You can't play alone.")
    } else if (nbrOfCards <= 1) {
      dom.window.alert("The maximum number of cards should be at least 2.")
    } else {
      dom.window.alert("Not enough players for the number of cards in hand.")
    }
  }

  private var _gameId: Long = 0

  def messageCallback(client: Client, msg: Message): Unit = {
    msg match {
      case GameLaunched(_, password, gameStateMessage) =>
        launchGameCallback(password, gameStateMessage)

      case CancelGame(_) =>
        cancelGameCallback()

      case NbrCardsInHand(_, n) =>
        currentNbrCards = n
        dom.document.getElementById("nbrCards").asInstanceOf[html.Label].innerHTML =
          s"Maximum number of cards in hand (currently $n)"
        updateNbrCardsWarning()

      case DeckMessage(_, _, _) =>

      case CurrentPlayers(_, names) =>
        updatePlayerList(names)
        updateNbrCardsWarning()

      case GameCreated(_, id) =>
        _gameId = id
        println("Game has been created successfully")
        sendReliable(NewPlayer(gameName, name, 0))

      case _ =>
        println(s"Un managed message: $msg")
    }
  }

  def connectedCallback(client: Client, peer: Peer, connected: Boolean): Unit = {
    if (connected) {
      sendReliable(NewGameCreation(gameName, name, registrationId))
    } else {
      if (!gameWasLaunched) println("I have been disconnected from the GameServer :(")
    }
  }

}
