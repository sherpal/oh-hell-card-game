package gamelogic

import networkcom._


/**
 * A GameAction is a way to go from one [[GameState]] to the next one.
 */
trait GameAction {
  def apply(gameState: GameState): GameState

  def toMessage(gameName: String): InGameMessage
}


case class PlayCard(player: String, card: Card) extends GameAction {

  def apply(gameState: GameState): GameState = {
    val gs = new GameState(
      gameState.players,
      gameState.lastTrick,
      gameState.points,
      gameState.bets,
      gameState.playedCards + (player -> card),
      gameState.hands + (player -> gameState.hands(player).filterNot(_ == card)),
      gameState.tricks,
      gameState.trumpCard,
      gameState.nbrCardsDistributed,
      gameState.maxNbrCards,
      gameState.nbrOfPerformedActions + 1
    )
    if (gs.playedCards.size == gs.players.length) {
      // need to go to next game hand or deal
      val winner = gs.currentWinner.get
      val nextHand = new GameState(
        gs.players,
        Some(gs.playedCardsInOrder),
        gs.points,
        gs.bets,
        Map[String, Card](),
        gs.hands,
        gs.tricks + (winner._1 -> (gameState.tricks(winner._1) + 1)),
        gs.trumpCard,
        gs.nbrCardsDistributed,
        gs.maxNbrCards,
        gs.nbrOfPerformedActions
      )
      if (nextHand.hands.values.forall(_.isEmpty)) { // the deal ended
        val newMaxNbr = if (nextHand.nbrCardsDistributed == nextHand.maxNbrCards) 0 else nextHand.maxNbrCards
        val newNbrCards = if (nextHand.nbrCardsDistributed >= newMaxNbr)
          nextHand.nbrCardsDistributed - 1 else nextHand.nbrCardsDistributed + 1
        new GameState(
          nextHand.players.tail :+ nextHand.players.head,
          None,
          nextHand.players.map(player => {
            (player, nextHand.points(player) + (if (nextHand.bets(player) == nextHand.tricks(player))
              nextHand.tricks(player) + 10
            else
              -math.abs(nextHand.tricks(player) - nextHand.bets(player))))
          }).toMap,
          Map[String, Int](),
          Map[String, Card](),
          Map[String, Set[Card]](),
          Map[String, Int](),
          None,
          newNbrCards,
          newMaxNbr,
          nextHand.nbrOfPerformedActions
        )
      } else {
        nextHand
      }
    } else {
      gs
    }
  }


  def toMessage(gameName: String): InGameMessage = PlayCardMessage(
    gameName, player, CardMessage(card.value.value, card.color.color)
  )
}

case class BetTrickNumber(player: String, bet: Int) extends GameAction {

  def apply(gameState: GameState): GameState = {
    new GameState(
      gameState.players,
      gameState.lastTrick,
      gameState.points,
      gameState.bets + (player -> bet),
      gameState.playedCards,
      gameState.hands,
      gameState.tricks,
      gameState.trumpCard,
      gameState.nbrCardsDistributed,
      gameState.maxNbrCards,
      gameState.nbrOfPerformedActions + 1
    )
  }

  def toMessage(gameName: String): InGameMessage = BetTrickNumberMessage(gameName, player, bet)
}


case class PlayerReceivesHand(player: String, cards: Array[Card]) extends GameAction {

  def apply(gameState: GameState): GameState = {
    new GameState(
      gameState.players,
      gameState.lastTrick,
      gameState.points,
      gameState.bets,
      gameState.playedCards,
      gameState.hands + (player -> cards.toSet),
      gameState.tricks + (player -> 0),
      gameState.trumpCard,
      gameState.nbrCardsDistributed,
      gameState.maxNbrCards,
      gameState.nbrOfPerformedActions + 1
    )
  }

  def toMessage(gameName: String): InGameMessage = PlayerReceivesHandMessage(
    gameName, player, cards.map(card => CardMessage(card.value.value, card.color.color))
  )
}


case class ChooseTrump(card: Card) extends GameAction {

  def apply(gameState: GameState): GameState = {
    new GameState(
      gameState.players,
      gameState.lastTrick,
      gameState.points,
      gameState.bets,
      gameState.playedCards,
      gameState.hands,
      gameState.tricks,
      Some(card),
      gameState.nbrCardsDistributed,
      gameState.maxNbrCards,
      gameState.nbrOfPerformedActions + 1
    )
  }

  def toMessage(gameName: String): InGameMessage = ChooseTrumpMessage(
    gameName, CardMessage(card.value.value, card.color.color)
  )
}

