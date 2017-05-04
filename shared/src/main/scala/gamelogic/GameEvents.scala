package gamelogic

import gui._

/**
 * Here we create all the gui events that relate to the game.
 */
object GameEvents {

  val onChooseTrump: ScriptKind { type Handler = (Frame, Card) => Unit } =
    ScriptKind.makeEvent[(Frame, Card) => Unit]

  val onPlayerPlaysCard: ScriptKind { type Handler = (Frame, String, Card) => Unit } =
    ScriptKind.makeEvent[(Frame, String, Card) => Unit]

  val onPlayerReceivesHand: ScriptKind { type Handler = (Frame, String, List[Card]) => Unit } =
    ScriptKind.makeEvent[(Frame, String, List[Card]) => Unit]

  val onPlayerBets: ScriptKind { type Handler = (Frame, String, Int) => Unit } =
    ScriptKind.makeEvent[(Frame, String, Int) => Unit]

  val onPlayerWinsTrick: ScriptKind { type Handler = (Frame, String) => Unit } =
    ScriptKind.makeEvent[(Frame, String) => Unit]

  val onPlayersStartBets: ScriptKind { type Handler = (Frame) => Unit } =
    ScriptKind.makeEvent[(Frame) => Unit]

  val onActionTaken: ScriptKind { type Handler = (Frame, GameState, GameAction) => Unit } =
    ScriptKind.makeEvent[(Frame, GameState, GameAction) => Unit]
}
