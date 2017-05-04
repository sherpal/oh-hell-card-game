package guiobjects

import gamelogic.{Card, GameAction, GameEvents, GameState}
import graphics.CardGraphics
import gui._

/**
 * Small Square on the screen that allow to view last trick.
 *
 * To view trick, player will have to put their mouse on the square.
 */
class LastTrickViewer(playerFrames: Set[PlayerFrame]) extends Frame(UIParent) {

  setSize(200, 50)
  setPoint(BottomRight)
  setFrameStrata(High)

  hide()

  private val background = createTexture()
  background.setAllPoints()
  background.setVertexColor(0,0,0)

  private val text = createFontString()
  text.setAllPoints()
  text.setText("View last trick")


  private val allCards: List[CardGraphics] = Card.defaultDeck.map(new CardGraphics(_))

  private var lastTrick: Option[Vector[(String, Card)]] = None

  private var visible: Boolean = false

  override def drawChildren(): Unit = {
    if (visible) {
      if (lastTrick.isDefined) {
        playerFrames.foreach((frame: PlayerFrame) => {
          val played = allCards.find(_.card == lastTrick.get.find(_._1 == frame.player).get._2).get
          played.setCenter(frame.right - played.width / 2, frame.bottom - played.height / 2 - 10)
          played.drawCard()
          frame.setPlaceInTrickTextAndShow(
            (lastTrick.get.indexWhere(_._1 == frame.player) + 1).toString,
            visible = true
          )
        })
      } else {
        playerFrames.foreach(_.setPlaceInTrickTextAndShow("", visible = false))
      }
    }

    super.drawChildren()
  }

  setScript(ScriptKind.OnEnter)((_: Frame, _: Frame) => {
    visible = true
  })

  setScript(ScriptKind.OnLeave)((_: Frame, _: Frame) => {
    visible = false
    playerFrames.foreach(_.setPlaceInTrickTextAndShow("", visible = false))
  })

  registerEvent(GameEvents.onActionTaken)((_: Frame, gameState: GameState, _: GameAction) => {
    lastTrick = gameState.lastTrick
  })
}
