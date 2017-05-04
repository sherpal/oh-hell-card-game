package gamelogic

import gamelogic.GameEvents._
import gui._

/**
 * GameLogs is there as debugging purposes.
 *
 * It will register to all Events and just prints their content.
 */
object GameLogs extends Frame() {

  registerEvent(onPlayerPlaysCard)((_: Frame, player: String, card: Card) => {
    println(player + " plays " + card.toString)
  })

  registerEvent(onPlayerBets)((_: Frame, player: String, bet: Int) => {
    println(player + " bets on " + bet + " tricks.")
  })

  registerEvent(onPlayerReceivesHand)((_: Frame, player: String, _: List[Card]) => {
    println(s"$player received a new hand.")
  })

  registerEvent(onPlayersStartBets)((_: Frame) => {
    println("Player starting bets")
  })

}
