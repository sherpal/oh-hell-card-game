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
    new GameState(
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
  }


  def toMessage(gameName: String): InGameMessage = PlayCardMessage(
    gameName, player, CardMessage(card.value.value, card.color.color)
  )
}

case class NewHand() extends GameAction {
  def apply(gameState: GameState): GameState = {
    val winner = gameState.currentWinner.get
    new GameState(
      gameState.players,
      Some(gameState.playedCardsInOrder),
      gameState.points,
      gameState.bets,
      Map[String, Card](),
      gameState.hands,
      gameState.tricks + (winner._1 -> (gameState.tricks(winner._1) + 1)),
      gameState.trumpCard,
      gameState.nbrCardsDistributed,
      gameState.maxNbrCards,
      gameState.nbrOfPerformedActions
    )
  }

  def toMessage(gameName: String): InGameMessage = NewHandMessage(gameName)
}

case class NewDeal(successBonus: Int, failurePenalty: Int,
                  bonusPerTrick: Int, penaltyPerTrick: Int) extends GameAction {

  private def computePoints(bets: Int, tricks: Int): Int = {
    if (bets == tricks)
      successBonus + bonusPerTrick * tricks
    else
      - failurePenalty - penaltyPerTrick * math.abs(bets - tricks)
  }

  def apply(gameState: GameState): GameState = {
    val winner = gameState.currentWinner.get
    val tricks = gameState.tricks + (winner._1 -> (gameState.tricks(winner._1) + 1))
    val newMaxNbr = if (gameState.nbrCardsDistributed == gameState.maxNbrCards) 0 else gameState.maxNbrCards
    val newNbrCards = if (gameState.nbrCardsDistributed >= newMaxNbr)
      gameState.nbrCardsDistributed - 1 else gameState.nbrCardsDistributed + 1


    new GameState(
      gameState.players.tail :+ gameState.players.head,
      None,
      gameState.players.map(player => {
        (player, gameState.points(player) + computePoints(gameState.bets(player), tricks(player)))
      }).toMap,
      Map[String, Int](),
      Map[String, Card](),
      Map[String, Set[Card]](),
      Map[String, Int](),
      None,
      newNbrCards,
      newMaxNbr,
      gameState.nbrOfPerformedActions
    )
  }

  def toMessage(gameName: String): InGameMessage = NewDealMessage(
    gameName, successBonus, failurePenalty, bonusPerTrick, penaltyPerTrick
  )
}

case class BetTrickNumber(player: String, bet: Int) extends GameAction {

  def apply(gameState: GameState): GameState = {
    val bets = gameState.bets + (player -> bet)
    val tricks = if (bets.size == gameState.players.size) gameState.players.map((_, 0)).toMap else gameState.tricks
    new GameState(
      gameState.players,
      gameState.lastTrick,
      gameState.points,
      bets,
      gameState.playedCards,
      gameState.hands,
      tricks,
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
      gameState.tricks,
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

