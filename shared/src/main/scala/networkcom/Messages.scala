package networkcom

import boopickle.Default._
import boopickle.CompositePickler

sealed abstract class Message


object Message {
  implicit val messagePickler: CompositePickler[Message] = compositePickler[Message]
    .addConcreteType[Connect]
    .addConcreteType[Connected]
    .addConcreteType[Disconnect]
    .addConcreteType[Disconnected]
    .addConcreteType[TestMessage]
    .addConcreteType[TestSendArray]

    .addConcreteType[ChatMessage]

    .addConcreteType[NewGameCreation]
    .addConcreteType[GameCreated]
    .addConcreteType[GameWasNotCreated]
    .addConcreteType[ReservePlayerName]
    .addConcreteType[PlayerNameReserved]
    .addConcreteType[ReserveGameName]
    .addConcreteType[GameNameReserved]
    .addConcreteType[GameDoesNotExist]

    .addConcreteType[CancelGame]
    .addConcreteType[LeaveGame]
    .addConcreteType[LaunchGame]
    .addConcreteType[GameLaunched]
    .addConcreteType[NewPlayer]
    .addConcreteType[CurrentPlayers]
    .addConcreteType[NbrCardsInHand]
    .addConcreteType[DeckMessage]

    .addConcreteType[GameCreationChatMessage]

    .addConcreteType[PlayerConnecting]
    .addConcreteType[StillWaitingForPlayers]
    .addConcreteType[GameStarts]
    .addConcreteType[CardMessage]

    .addConcreteType[PlayCardMessage]
    .addConcreteType[NewHandMessage]
    .addConcreteType[NewDealMessage]
    .addConcreteType[PlayRandomCard]
    .addConcreteType[BetTrickNumberMessage]
    .addConcreteType[PlayerReceivesHandMessage]
    .addConcreteType[ChooseTrumpMessage]

    .addConcreteType[InGameChatMessage]

    .addConcreteType[ClosingGame]

    .addConcreteType[GameStateMessage]

}

final case class Connect() extends Message
final case class Connected() extends Message
final case class Disconnect() extends Message
final case class Disconnected() extends Message
final case class TestMessage(s: String) extends Message
final case class TestSendArray(strings: Array[String]) extends Message
final case class ChatMessage(s: String, time: Long, sender: String) extends Message


trait ChatMessageType extends Message {
  val gameName: String
  val time: Long
  val s: String
  val sender: String
}

/**
 * These messages are sent in the GameMenu, while someone wants either to host a game, or to join one.
 */
trait PreGameMessage extends Message
final case class NewGameCreation(gameName: String, hostName: String, registrationId: Int) extends PreGameMessage
final case class GameCreated(gameName: String, id: Long) extends PreGameMessage
final case class GameWasNotCreated(gameName: String) extends PreGameMessage
final case class ReservePlayerName(gameName: String, playerName: String) extends PreGameMessage
final case class PlayerNameReserved(gameName: String, playerName: String, id: Int, success: Boolean) extends PreGameMessage
final case class ReserveGameName(gameName: String) extends PreGameMessage
final case class GameNameReserved(gameName: String, id: Int, success: Boolean) extends PreGameMessage
final case class GameDoesNotExist(gameName: String) extends PreGameMessage

/**
 * These messages are sent before a game starts, while players join a game hosted by someone.
 * The host can also set the settings of the game.
 */
trait GameCreationMessage extends Message
final case class CancelGame(gameName: String) extends GameCreationMessage
final case class LeaveGame(gameName: String, playerName: String) extends GameCreationMessage
final case class LaunchGame(gameName: String) extends GameCreationMessage
final case class GameLaunched(gameName: String, password: Int, gameState: GameStateMessage) extends GameCreationMessage
final case class NewPlayer(gameName: String, playerName: String, reservationId: Int) extends GameCreationMessage
final case class CurrentPlayers(gameId: Long, playerNames: Array[String]) extends GameCreationMessage
final case class NbrCardsInHand(gameName: String, n: Int) extends GameCreationMessage
final case class DeckMessage(gameName: String, cardsPerColor: Int, colors: Int) extends GameCreationMessage
final case class GameCreationChatMessage(gameName: String, s: String, time: Long, sender: String)
  extends GameCreationMessage with ChatMessageType

/**
 * These messages are sent during the game.
 * Either the server tells the clients something happen, or the clients communicate their actions.
 */
trait InGameMessage extends Message {
  val gameName: String
}
final case class PlayerConnecting(gameName: String, playerName: String, password: Int) extends InGameMessage
final case class StillWaitingForPlayers(gameName: String, n: Int) extends InGameMessage
final case class GameStarts(gameName: String, nbrCardsInHand: Int) extends InGameMessage
final case class CardMessage(value: Int, color: String) extends Message

final case class PlayCardMessage(gameName: String, playerName: String, card: CardMessage) extends InGameMessage
final case class NewHandMessage(gameName: String) extends InGameMessage
final case class NewDealMessage(gameName: String, successBonus: Int, failurePenalty: Int,
                               bonusPerTrick: Int, penaltyPerTrick: Int) extends InGameMessage
final case class PlayRandomCard(gameName: String, playerName: String) extends InGameMessage
final case class BetTrickNumberMessage(gameName: String, playerName: String, bet: Int) extends InGameMessage
final case class PlayerReceivesHandMessage(gameName: String, playerName: String, cards: Array[CardMessage]) extends
InGameMessage
final case class ChooseTrumpMessage(gameName: String, card: CardMessage) extends InGameMessage

final case class ClosingGame(gameName: String, msg: String) extends InGameMessage

final case class InGameChatMessage(gameName: String, s: String, time: Long, sender: String)
  extends InGameMessage with ChatMessageType

final case class GameStateMessage(
                                 players: Vector[String],
                                 lastTrick: Option[Vector[(String, CardMessage)]],
                                 points: Map[String, Int],
                                 bets: Map[String, Int],
                                 playedCards: Map[String, CardMessage],
                                 hands: Map[String, Set[CardMessage]],
                                 tricks: Map[String, Int],
                                 trumpCard: Option[CardMessage],
                                 nbrCardsDistributed: Int,
                                 maxNbrCards: Int,
                                 nbrOfPerformedActions: Int
                                 ) extends Message

