package gamelogic

import exceptions.GameAlreadyStarted
import gameserver.GameServer
import networkcom.Peer

import scala.collection.mutable


/**
 * A GameCreation instance will gather all information needed to launch a Rikiki Game such as
 * - players
 * - deck used
 * - maximal number of cards players will have in a Deal
 */
class GameCreation(val name: String, val hostName: String, hostPeer: Peer, server: GameServer) {

  val id: Long = GameCreation.getId

  private val players: mutable.Map[String, Peer] = mutable.Map()

  def containsPlayer(player: String): Boolean = players.keys.toList.contains(player)

  def currentPlayers: Array[String] = players.keys.toArray

  def currentPlayersWithPeers: Map[String, Peer] = players.toMap

  def addNewPlayer(playerName: String, peer: Peer, id: Int): Boolean = {
    if (gameStarted) throw GameAlreadyStarted()
    else if (bookedPlayerNames.isDefinedAt(id) && bookedPlayerNames(id) == playerName) {
      players += (playerName -> peer)
      true
    } else
      false
  }

  def removePlayer(player: String): Boolean = {
    if (gameStarted) throw GameAlreadyStarted()
    else {
      players -= player
      bookedPlayerNames.clone.find(elem => elem._2 == player) match {
        case Some((reservationID, _)) =>
          bookedPlayerNames -= reservationID
        case None =>
      }
      player == hostName
    }
  }

  private val bookedPlayerNames: mutable.Map[Int, String] = mutable.Map(0 -> hostName)
  def getBookedPlayerNames: Set[(Int, String)] = bookedPlayerNames.toSet

  def bookName(name: String, id: Int): Boolean = {
    if (bookedPlayerNames.values.toList.contains(name)) false
    else {
      bookedPlayerNames += (id -> name)
      true
    }
  }

  def unBookName(id: Int): Unit =
    bookedPlayerNames -= id

  private val deck: mutable.Set[Card] = mutable.Set()
  def currentDeck: Set[Card] = deck.toSet

  private var _deckComposition: (Int, Int) = (13, 4)
  def deckComposition: (Int, Int) = _deckComposition

  def setDeck(cardsPerColor: Int, colors: Int): Unit = {
    _deckComposition = (cardsPerColor, colors)
    deck.clear()
    if (cardsPerColor == 13 && colors == 4)
      Card.defaultDeck.foreach(deck += _)
    else
      println("We do not allow this for now.")
  }


  private var _maxNbrCardsInHand: Int = 1
  def maxNbrCardsInHand: Int = _maxNbrCardsInHand

  def setMaxNbrCardsInHand(n: Int): Unit =
    _maxNbrCardsInHand = n

  private var gameStarted: Boolean = false
  def hasStarted: Boolean = gameStarted

  def launchGame(password: Int): GamePlaying = {
    gameStarted = true
    new GamePlaying(name, password, players.keys.toVector, maxNbrCardsInHand, server)
  }
}


object GameCreation {

  private val idQueue: mutable.Queue[Long] = mutable.Queue()

  private var lastId: Long = 0

  def getId: Long = if (idQueue.isEmpty) {
    lastId += 1
    lastId
  } else {
    idQueue.dequeue()
  }

  def freeId(id: Long): Unit =
    idQueue.enqueue(id)
}
