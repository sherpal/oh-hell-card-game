package gameserver

import exceptions.DoesNotManageThisMessage
import gamelogic._
import networkcom._
import org.scalajs.dom
import org.scalajs.dom.{Element, html}

import scala.scalajs.js.timers.setInterval
import scala.collection.mutable



/**
 * A GameServer runs on a dedicated BrowserWindow and will manage all communication to and from the Players.
 *
 */
class GameServer(val address: String, val port: Int) extends Server {


  setInterval(2000) {
    games.keys.filterNot(games(_).hasStarted).foreach(sendGameInfo)
    updatePlayerListElement()
  }

  /**
   * In this section, we manage everything that will happen before a game effectively launch.
   */
  private val games: mutable.Map[Long, GameCreation] = mutable.Map()
  private val gameIds: mutable.Map[String, Long] = mutable.Map()


  private def cancelGame(gameId: Long): Unit = if (games.isDefinedAt(gameId)) {
    bookedGameName.find(_._2 == games(gameId).name) match {
      case Some((key, _)) => bookedGameName -= key
      case _ =>
    }

    sendReliableToGameMembers(gameId, CancelGame(games(gameId).name))
    gameIds -= games(gameId).name
    games -= gameId
    GameCreation.freeId(gameId)
  }

  private def playerDisconnectedPreLaunch(gameId: Long, playerName: String): Unit = {
    if (games.isDefinedAt(gameId) && games(gameId).containsPlayer(playerName)) {
      val game = games(gameId)

      if (playerName == game.hostName) {
        cancelGame(gameId)
      } else {
        if (clients.contains(game.currentPlayersWithPeers(playerName)))
          sendReliable(LeaveGame(game.name, playerName), game.currentPlayersWithPeers(playerName))

        game.removePlayer(playerName)
        sendGameInfo(gameId)
      }
      updatePlayerListElement()
    }
  }


  dom.document.getElementById("serverAddress").asInstanceOf[html.Heading].textContent = s"$address:$port"


  private val playerListHTML: html.UList = dom.document.getElementById("playerList").asInstanceOf[html.UList]
  private def playerElementsList: IndexedSeq[Element] = {
    val children = playerListHTML.children
    (0 until children.length).map(children(_))
  }

  private def allConnectedPlayers: Set[String] = games.values.flatMap(_.currentPlayers).toSet

  private def updatePlayerListElement(): Unit = {
    val allElements = playerElementsList.toSet
    val allConnected = allConnectedPlayers

    allElements
      .filterNot(elem => allConnected.contains(elem.innerHTML))
      .foreach(elem => elem.parentNode.removeChild(elem))

    allConnected
      .diff(allElements.map(_.innerHTML))
      .foreach(addPlayerToHTMLList)
  }

  private def addPlayerToHTMLList(playerName: String): Unit = {
    val li = dom.document.createElement("li").asInstanceOf[html.LI]
    playerListHTML.appendChild(li)
    li.textContent = playerName
  }

  def sendReliableToGameMembers(gameId: Long, message: Message): Unit = {
    val currentPlayers = games(gameId).currentPlayersWithPeers.toSet
    currentPlayers.map(_._2).intersect(clients).foreach(sendReliable(message, _))
  }

  def sendGameInfo(gameId: Long): Unit = {
    val game = games(gameId)
    sendReliableToGameMembers(gameId, CurrentPlayers(gameId, game.currentPlayers))
    sendReliableToGameMembers(gameId, DeckMessage(game.name, game.deckComposition._1, game.deckComposition._2))
    sendReliableToGameMembers(gameId, NbrCardsInHand(game.name, game.maxNbrCardsInHand))
  }


  private val bookedGameName: mutable.Map[Int, String] = mutable.Map()

  def reserveGameName(name: String, reservationId: Int): Boolean =
    if (isGameNameFree(name)) {
      bookedGameName += (reservationId -> name)
      true
    } else false

  def isGameNameFree(gameName: String): Boolean =
    !(bookedGameName.values.toSet.contains(gameName) || games.values.toSet.contains(gameName))


  /**
   * Manages the communication with a PreGameClient client, before creating or joining a game.
   *
   * The goal is to have access to information like existence of some game, existence of player name in some game...
   */
  private def preGameCallback(m: Message, peer: Peer): Unit = {
    m match {
      case ReserveGameName(gameName) =>
        var success: Boolean = false
        var reservationId: Int = 0
        if (isGameNameFree(gameName)) {
          reservationId = scala.util.Random.nextInt
          reserveGameName(gameName, reservationId)
          success = true
        }
        sendReliable(GameNameReserved(gameName, reservationId, success), peer)

      case ReservePlayerName(gameName, name) =>
        gameIds.get(gameName) match {
          case Some(id) =>
            if (games.isDefinedAt(id)) {
              val reservationID = scala.util.Random.nextInt
              val success = games(id).bookName(name, reservationID)
              sendReliable(PlayerNameReserved(gameName, name, reservationID, success), peer)
            } else {
              sendReliable(GameDoesNotExist(gameName), peer)
            }
          case None =>
            sendReliable(GameDoesNotExist(gameName), peer)
        }

      case NewGameCreation(name, hostName, registrationId) =>
        bookedGameName.get(registrationId) match {
          case Some(gName) if gName == name =>
            val gameCreation = new GameCreation(name, hostName, peer, this)
            games += (gameCreation.id -> gameCreation)
            gameIds += (name -> gameCreation.id)
            sendReliable(GameCreated(name, gameCreation.id), peer)
          case _ =>
            sendReliable(GameWasNotCreated(name), peer) // should never happen
        }

      case _ =>
        throw DoesNotManageThisMessage(s"Message class was ${m.getClass}.")
    }
  }


  /**
   * Manages game settings and player connections.
   */
  private def gameCreationCallback(m: Message, peer: Peer): Unit = {
    m match {
      case NbrCardsInHand(gameName, n) =>
        gameIds.get(gameName) match {
          case Some(id) =>
            if (games.isDefinedAt(id)) {
              games(id).setMaxNbrCardsInHand(n)
              sendGameInfo(id)
            }
          case None =>
        }

      case LaunchGame(gameName) =>
        gameIds.get(gameName) match {
          case Some(id) =>
            val password: Int = scala.util.Random.nextInt
            gamesPlaying += (gameName -> games(id).launchGame(password))
            sendReliableToGameMembers(id, GameLaunched(
              gameName, password,
              // at this point, the currentGameState is the original game state
              gamesPlaying(gameName).currentGameState.toMessage
            ))
          case _ =>
        }

      case LeaveGame(gameName, playerName) =>
        gameIds.get(gameName) match {
          case Some(id) =>
            playerDisconnectedPreLaunch(id, playerName)
          case _ =>
        }

      case DeckMessage(gameName, cardsPerColor, colors) =>
        gameIds.get(gameName) match {
          case Some(id) =>
            if (games.isDefinedAt(id)) {
              games(id).setDeck(cardsPerColor, colors)
              sendGameInfo(id)
            }
          case None =>
        }

      case NewPlayer(gameName, name, registrationId) =>
        gameIds.get(gameName) match {
          case Some(id) =>
            if (games.isDefinedAt(id) && !games(id).containsPlayer(name)) {
              val success = games(id).addNewPlayer(name, peer, registrationId)
              if (success) {
                updatePlayerListElement()
              }
              sendGameInfo(id)
            }
          case None =>
        }


      case _ =>
        throw DoesNotManageThisMessage(s"Message class was ${m.getClass}.")
    }
  }


  /**
   * This section will manage everything that happens during a game.
   *
   * At this point, the game has been created and we wait for the players to connect for the game.
   * We know who should arrive, and we will wait until every one is back.
   * If someone does not show up within 10 seconds, the game will be canceled.
   */

  private val gamesPlaying: mutable.Map[String, GamePlaying] = mutable.Map()

  private def inGameCallback(message: Message, peer: Peer): Unit = {
    message match {
      case m: InGameMessage =>
        gamesPlaying.get(m.gameName) match {
          case Some(game) => game.messageCallback(m, peer)
          case _ =>
        }
      case _ =>
        throw DoesNotManageThisMessage(s"Message class was ${message.getClass}.")
    }
  }

  def closePlayingGame(name: String, disconnectedPlayer: String): Unit = gamesPlaying.get(name) match {
    case Some(game) =>
      cancelGame(gameIds(name))
      game.closeGame(s"Player $disconnectedPlayer has been disconnected.")
      gamesPlaying -= name
    case None =>
  }


  /**
   * This section is used to dispatch incoming messages in the relevant section.
   */


  def messageCallback(server: Server, peer: Peer, m: Message): Unit = {
    m match {
      case m: GameCreationMessage => gameCreationCallback(m, peer)
      case m: PreGameMessage => preGameCallback(m, peer)
      case m: InGameMessage => inGameCallback(m, peer)
      case TestMessage(msg: String) =>
        println("TestMessage received: " + msg)
      case _ =>
        println(s"Unknown message type: $m")
    }
  }

  def clientConnectedCallback(server: Server, peer: Peer, connected: Boolean): Unit = {
      if (connected) {
        //sendReliable(TestMessage("welcome"), peer)
      } else {
        games.values.find(_.currentPlayersWithPeers.values.exists(_ == peer)) match {
          case Some(game) if !game.hasStarted =>
            val (player, _) = game.currentPlayersWithPeers.find(_._2 == peer).get
            playerDisconnectedPreLaunch(game.id, player)
          case Some(_) =>
          case _ =>
        }
        gamesPlaying.values.find(_.playersPeers.exists(_ == peer)) match {
          case Some(game) =>
            closePlayingGame(game.gameName, game.peerToPlayer(peer))
          case None =>
        }
      }

      updatePlayerListElement()
    }


}
