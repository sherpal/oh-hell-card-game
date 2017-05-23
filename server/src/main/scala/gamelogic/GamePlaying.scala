package gamelogic

import gameserver.GameServer
import networkcom._

import scala.collection.mutable

import scala.scalajs.js.timers.setTimeout

/**
 * A Game Hosted by a server.
 * Manages all communications and game states during a game.
 */
class GamePlaying(val gameName: String, val password: Int,
                  val players: Vector[String], val maxNbrCards: Int,
                  val server: GameServer) {


  def closeGame(msg: String): Unit = {
    broadcastReliable(ClosingGame(gameName, msg))
  }

  private var _state: GamePlayingState = WaitingForPlayers

  def state: GamePlayingState = _state

  private val playersWithPeers: mutable.Map[String, Peer] = mutable.Map()
  def playersPeers: Iterable[Peer] = playersWithPeers.values
  def peerToPlayer: Map[Peer, String] = playersWithPeers.toSet.map(
    (elem: (String, Peer)) => (elem._2, elem._1)
  ).toMap

  private def broadcastReliable(message: InGameMessage): Unit = {
    playersWithPeers.values.foreach(server.sendReliable(message, _))
  }

  private def broadcastOrderedReliable(message: InGameMessage): Unit = {
    playersWithPeers.values.foreach(server.sendOrderedReliable(message, _))
  }

  private def broadcastOrderedReliableButOne(message: InGameMessage, player: String): Unit = {
    playersWithPeers.filterNot(_._1 == player).values.foreach(server.sendOrderedReliable(message, _))
  }

  private def cardMessage2Card(cardMessage: CardMessage): Card =
    DefaultCard(DefaultCardValue(cardMessage.value), CardColor(cardMessage.color))

  def messageCallback(message: InGameMessage, peer: Peer): Unit = if (state != GamePlayingEnded) {
    message match {
      case PlayRandomCard(_, player) =>
        val gameState = currentGameState
        if (gameState.state == PlayingCardState && !gameState.turnOfPlayer._2 && gameState.turnOfPlayer._1 == player) {
          val legalActions = gameState.legalActions
          // if it is the turn of player player, their hand is not empty, so needless to check
          val card = scala.util.Random.shuffle(
            gameState
            .hands(player)
            .filter(card => legalActions(PlayCard(player, card)))
            .toList
          ).head
          val action = PlayCard(player, card)
          actions :+= action
          broadcastOrderedReliable(PlayCardMessage(gameName, player, CardMessage(card.value.value, card.color.color)))
          performAction(action(gameState))
        }

      case PlayCardMessage(_, player, cardMessage) =>
        val action = PlayCard(player, cardMessage2Card(cardMessage))
        val gameState = currentGameState
        if (gameState.legalActions.apply(action)) {
          actions :+= action
          broadcastOrderedReliable(PlayCardMessage(gameName, player, cardMessage))
          performAction(action(gameState))
        } else {
          println(s"Received card $cardMessage from player $player but not their turn.")
        }
      case BetTrickNumberMessage(_, player, bet) =>
        val action = BetTrickNumber(player, bet)
        val gameState = currentGameState
        if (gameState.legalActions.apply(action)) {
          actions :+= action
          broadcastOrderedReliable(BetTrickNumberMessage(gameName, player, bet))
          performAction(action(gameState))
        } else {
          println(s"Received bet $bet of player $player but not their turn.")
        }
      case PlayerConnecting(gName, pName, pw) if gameName == gName && password == pw && players.contains(pName)
      =>
        playersWithPeers += (players.find(_ == pName).get -> peer)
        val stillWaitFor = players.size - playersWithPeers.size
        if (stillWaitFor > 0) {
          broadcastOrderedReliable(StillWaitingForPlayers(gameName, stillWaitFor))
        } else {
          broadcastOrderedReliable(GameStarts(gameName, maxNbrCards))
          _state = Playing
          setTimeout(1000) {
            performAction()
          }
        }

      case InGameChatMessage(g, s, _, p) =>
        if (s.trim != "") {
          // the time of the message is the instant at which the server receives it
          broadcastReliable(InGameChatMessage(g, s, new java.util.Date().getTime, p))
        }

      case _ =>
        println(s"Unknown message: $message")
    }
  }


  private val originalGameState: GameState = GameState.originalState(
    scala.util.Random.shuffle(players), maxNbrCards
  )

  private var actions: List[GameAction] = Nil

  def currentGameState: GameState = originalGameState(actions)

  private var allCards: List[Card] = Card.shuffledDefaultDeck

  def remainingCards(gameState: GameState = currentGameState): List[Card] = {
    allCards.filterNot(gameState.distributedCards.contains)
  }

  private def shuffleCards(): Unit = {
    allCards = scala.util.Random.shuffle(allCards)
  }

  private def givePlayerAHand(player: String, gameState: GameState): PlayerReceivesHand = {
    val remaining = remainingCards(gameState)
    PlayerReceivesHand(player, remaining.take(gameState.nbrCardsDistributed).toArray)
  }

  /**
   * These four numbers are used to count points during the game.
   * In a future version, maybe we'll allow the game host to chose these numbers in order to customize the game further.
   */
  private val successBonus: Int = 10
  private val failurePenalty: Int = 0
  private val bonusPerTrick: Int = 1
  private val penaltyPerTrick: Int = 1

  private def chooseTrump(gameState: GameState): GameAction = ChooseTrump(remainingCards(gameState).head)

  private def performAction(gameState: GameState = currentGameState): Unit = {
    gameState.state match {
      case GameEnded =>
        println(s"Game has ended, winner is ${gameState.points.toList.maxBy(_._2)._1}")
        setTimeout(1000) {
          closeGame("gameEndedNormally")
        }

      case DistributingCardState =>
        val action = givePlayerAHand(gameState.players.filterNot(gameState.hands.keys.toSet.contains).head, gameState)
        val nbrCardsDistributed = gameState.nbrCardsDistributed
        actions :+= action
        if (nbrCardsDistributed > 1) {
          server.sendOrderedReliable(action.toMessage(gameName), playersWithPeers(action.player))
        } else {
          broadcastOrderedReliableButOne(action.toMessage(gameName), action.player)
        }
        setTimeout(500) {performAction(action(gameState))}

      case ChoosingTrumpState =>
        val action = chooseTrump(gameState)
        actions :+= action
        broadcastOrderedReliable(action.toMessage(gameName))
        performAction(action(gameState))

      case BettingState =>
        shuffleCards() // shuffling, BettingState means that distributing is over, so we can shuffle.
        // we do nothing as we should wait for player input
      case PlayingCardState =>
        // we do nothing as we should wait for player input
      case NewDealState =>
        setTimeout(4000) {
          val action = NewDeal(successBonus, failurePenalty, bonusPerTrick, penaltyPerTrick)
          actions :+= action
          broadcastOrderedReliable(action.toMessage(gameName))
          setTimeout(500) {
            performAction(action(gameState))
          }
        }
      case NewHandState =>
        setTimeout(2000) {
          actions :+= NewHand()
          broadcastOrderedReliable(NewHand().toMessage(gameName))
          setTimeout(500) {
            performAction(NewHand()(gameState))
          }
        }
    }
  }

}


sealed trait GamePlayingState
case object WaitingForPlayers extends GamePlayingState
case object Playing extends GamePlayingState
case object GamePlayingEnded extends GamePlayingState
