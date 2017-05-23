package gamelogic

import networkcom.{CardMessage, GameStateMessage}

/**
 * A GameState records all information needed to continue the game at any point.
 *
 * It also had plenty of methods to obtain information that can be deduce from the initial properties. If you find a
 * relevant property of the game that can't be deduced from the GameState, then the design of the GameState is not
 * strong enough and has to be changed. (Remark: as an example of non relevant info, we have the hole history of the
 * tricks played, which is not needed to continue playing.)
 *
 * @param players             Vector of player name in the order they have to start the deal
 * @param lastTrick           content of the last trick, as Vector of (String, Card), in order they were played.
 * @param points              points the players currently have
 * @param bets                bets the players currently did (may be partial map if bets are ongoing)
 * @param playedCards         map of card played up to now by players (may be partial if betting or ongoing)
 * @param hands               the hands each player have at that point
 * @param tricks              map of the tricks each player has for this deal
 * @param trumpCard           the card that represents the trump (the trump value is then its color)
 * @param nbrCardsDistributed nbr of cards distributed at this deal
 * @param maxNbrCards         max number of cards that will be distributed this game, or 0 if we already got there
 * @param nbrOfPerformedActions number of performed actions since the original game state.
 */
class GameState (
                val players: Vector[String],
                val lastTrick: Option[Vector[(String, Card)]],
                val points: Map[String, Int],
                val bets: Map[String, Int],
                val playedCards: Map[String, Card],
                val hands: Map[String, Set[Card]],
                val tricks: Map[String, Int],
                val trumpCard: Option[Card],
                val nbrCardsDistributed: Int,
                val maxNbrCards: Int,
                val nbrOfPerformedActions: Int
                ) {

  override def toString: String = "Game State:\n" +
    s"Number of actions: $nbrOfPerformedActions\n" +
    players.map(player => {
      player + "\n" +
      s"\tbets: ${bets.getOrElse(player, "NA")}\n" +
      s"\tplayed card: ${playedCards.getOrElse(player, "NA")}\n" +
      s"\thand: ${hands.getOrElse(player, Set("NA")).mkString(", ")}\n" +
      s"\ttricks: ${tricks.getOrElse(player, "NA")}\n" +
      s"\tpoints: ${points.getOrElse(player, "NA")}"
    }).mkString("\n") + "\n" +
    s"trump card: $trumpCard\n" +
    s"max number of cards: $maxNbrCards\n" +
    s"cards distributed: $nbrCardsDistributed"

  def toMessage: GameStateMessage = GameStateMessage(
    players,
    lastTrick match {
      case None =>
        None
      case Some(trick) =>
        Some(trick.map({case (player, card) => (player, CardMessage(card.value.value, card.color.color))}))
    },
    points,
    bets,
    playedCards.map({case (player, card) => (player, CardMessage(card.value.value, card.color.color))}),
    hands.map({case (player, cards) => (player, cards.map(card => CardMessage(card.value.value, card.color.color)))}),
    tricks,
    trumpCard match {
      case None => None
      case Some(card) => Some(CardMessage(card.value.value, card.color.color))
    },
    nbrCardsDistributed,
    maxNbrCards,
    nbrOfPerformedActions
  )


  def apply(actions: Seq[GameAction]): GameState = actions.foldLeft(this)((gs: GameState, a: GameAction) => a(gs))

  def gameOver: Boolean = maxNbrCards == 0 && nbrCardsDistributed == 0

  def state: GameStateState = if (gameOver) GameEnded
  else if (hands.values.size < players.length) DistributingCardState
  else if (trumpCard.isEmpty) ChoosingTrumpState
  else if (bets.size < players.length) BettingState
  else if (playedCards.size == players.length && hands.values.forall(_.isEmpty)) NewDealState
  else if (playedCards.size == players.length) NewHandState
  else PlayingCardState

  /**
   * These legalActions only make sense for the Server, as the clients will believe the server when they are told
   * a GameAction.
   *
   * It still makes sense for clients to check whether it is their turn to play, which is useful to reduce the amount
   * of UDP traffic that goes around.
   *
   * @return a function from GameAction to Boolean, that returns true whether the action is legal.
   */
  def legalActions: GameAction => Boolean = state match {
    case DistributingCardState => {
      case action: PlayerReceivesHand =>
        action.cards.length == nbrCardsDistributed && // distributing the right amount of cards
          players.filterNot(hands.isDefinedAt).contains(action.player) && // player has not hand
          distributedCards.intersect(action.cards.toSet).isEmpty // do not distribute twice the same card
      case _ =>
        false
    }
    case ChoosingTrumpState => {
      case action: ChooseTrump =>
        !distributedCards.contains(action.card) // the trump may not have been distributed
      case _ =>
        false
    }
    case BettingState => {
      case action: BetTrickNumber =>
        turnOfPlayer._1 == action.player && // right player turn
        turnOfPlayer._2 && // betting state, should always be right
        action.bet >= 0 && // can't bet a negative number of tricks
        action.bet <= nbrCardsDistributed && // can't bet a number of tricks bigger that the distributed number
        (bets.size < players.length - 1 || bets.values.sum + action.bet != nbrCardsDistributed)
      case _ =>
        false
    }
    case NewHandState => {
      case _: NewHand => true
      case _ => false
    }
    case NewDealState => {
      case _: NewDeal => true
      case _ => false
    }
    case PlayingCardState => {
      case action: PlayCard =>
        players.contains(action.player) && // player plays the game
        action.player == turnOfPlayer._1 && // right player turn
        !turnOfPlayer._2 && // playing card state, should always be right at this point
        hands(action.player).contains(action.card) && // can only play a card that you have in hand
          (startingColor.isEmpty || action.card.color == startingColor.get ||
            hands(action.player).forall(_.color != startingColor.get))
      case _ =>
        false
    }
    case GameEnded =>
      (_: GameAction) => false
  }

  def distributedCards: Set[Card] = hands.values.fold(Set[Card]())(_ union _)

  def trump: CardColor = trumpCard.get.color

  def lastWinner: Option[String] = lastTrick match {
    case None => None
    case Some(vector) =>
      val color = vector.head._2.color
      Some(vector.tail.foldLeft(vector.head)({case ((p1, c1), (p2, c2)) =>
          if (c1.beat(c2, trump, color)) (p1, c1) else (p2, c2)
      })._1)
  }

  // first card in the Vector is the first card played
  def playedCardsInOrder: Vector[(String, Card)] = (lastWinner match {
    case None =>
      players
    case Some(player) =>
      val idx = players.indexOf(player)
      players.drop(idx) ++ players.take(idx)
  })
    .filter(playedCards.isDefinedAt)
    .map(player => (player, playedCards(player)))


  // true if it is betting stage, false if it is playing card stage
  def turnOfPlayer: (String, Boolean) = state match {
    case BettingState =>
      (players(bets.size), true)
    case PlayingCardState =>
      val nbrPlayedCards = playedCards.size
      lastWinner match {
        case Some(player) =>
          val idx = players.indexOf(player)
          (players((idx + nbrPlayedCards) % players.length), false)
        case None =>
          (players(nbrPlayedCards), false)
      }
    case _ =>
      ("", false)
  }
//  if (bets.size == players.length) {
//    // bets are finished, playing card stage
//
//    val nbrPlayedCards = playedCards.size
//    lastWinner match {
//      case Some(player) =>
//        val idx = players.indexOf(player)
//        (players((idx + nbrPlayedCards) % players.length), false)
//      case None =>
//        (players(nbrPlayedCards), false)
//    }
//  } else {
//    // betting stage
//    (players(bets.size), true)
//  }

  def startingColor: Option[CardColor] = if (playedCards.isEmpty) None else Some(
    playedCardsInOrder.head._2.color
  )

  def currentWinner: Option[(String, Card)] = if (playedCards.isEmpty) None else {
    val cardsWithPlayers = playedCardsInOrder
    Some(cardsWithPlayers.tail.foldLeft(cardsWithPlayers.head)({
      case ((p1, c1), (p2, c2)) => if (c1.beat(c2, trump, startingColor.get)) (p1, c1) else (p2, c2)
    }))
  }

}

object GameState {

  def originalState(players: Vector[String], maxNbrCards: Int): GameState = new GameState(
    players, None, players.map((_, 0)).toMap, Map[String, Int](), Map[String, Card](), Map[String, Set[Card]](),
    Map[String, Int](), None, 1, maxNbrCards, 0
  )

}


sealed trait GameStateState
case object DistributingCardState extends GameStateState
case object ChoosingTrumpState extends GameStateState
case object BettingState extends GameStateState
case object PlayingCardState extends GameStateState
case object NewHandState extends GameStateState
case object NewDealState extends GameStateState
case object GameEnded extends GameStateState
