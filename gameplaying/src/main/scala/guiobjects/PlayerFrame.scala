package guiobjects

import communication.PlayerClient
import gamelogic.{GameAction, GameEvents, GameState}
import gui._

/**
 * This represents a Player sitting at the table.
 */
class PlayerFrame(val player: String, playerClient: PlayerClient) extends Frame(UIParent) {

  setWidth(200)
  setHeight(50)

  private val border = createTexture(layer = Artwork)
  border.setAllPoints()
  border.setVertexColor(
    PlayerFrame.frontColor._1, PlayerFrame.frontColor._2, PlayerFrame.frontColor._3
  )
  border.setMode(LineMode)
  border.lineWidth = 3

  private val background = createTexture(layer = Artwork)
  background.setAllPoints()
  background.setVertexColor(
    PlayerFrame.backgroundColor._1, PlayerFrame.backgroundColor._2, PlayerFrame.backgroundColor._3, 0.7
  )

  private val playerName = createFontString()
  playerName.setPoint(TopLeft, this, TopLeft, 5, 0)
  playerName.setSize(150, 25)
  playerName.setText(player)
  playerName.setTextColor(
    PlayerFrame.frontColor._1, PlayerFrame.frontColor._2, PlayerFrame.frontColor._3
  )
  playerName.setJustifyH(JustifyLeft)


  private val playerPoints = createFontString()
  playerPoints.setPoint(TopRight, this, TopRight, -5, 0)
  playerPoints.setPoint(BottomLeft, playerName, BottomRight)
  playerPoints.setText(playerClient.currentGameState.points(player).toString)
  playerPoints.setTextColor(
    PlayerFrame.frontColor._1, PlayerFrame.frontColor._2, PlayerFrame.frontColor._3
  )
  playerPoints.setJustifyH(JustifyRight)

  private val placeInTrick = createFontString()
  placeInTrick.setPoint(BottomRight, this, BottomRight)
  placeInTrick.setSize(20, 20)
  placeInTrick.setText("")
  placeInTrick.setTextColor(0,0,0)

  /**
   * Used when hovering "View Last Trick" button.
   */
  def setPlaceInTrickTextAndShow(t: String, visible: Boolean): Unit = {
    placeInTrick.setText(t)
    if (visible) placeInTrick.show() else placeInTrick.hide()
  }

  private val tricks = createFontString()
  tricks.setPoint(BottomLeft, this, BottomLeft, 5, 0)
  tricks.setPoint(TopRight, playerPoints, BottomRight)
  setTrickText()
  tricks.setTextColor(
    PlayerFrame.frontColor._1, PlayerFrame.frontColor._2, PlayerFrame.frontColor._3
  )
  private def setTrickText(gameState: GameState = playerClient.currentGameState): Unit =
    tricks.setText(s"${gameState.tricks.getOrElse(player, "")} / " +
      s"${gameState.bets.getOrElse(player, "")}")


  def colorFocus(flag: Boolean): Unit = {
    if (flag) {
      border.setVertexColor(
        PlayerFrame.focusedColor._1, PlayerFrame.focusedColor._2, PlayerFrame.focusedColor._3
      )

    } else {
      border.setVertexColor(
        PlayerFrame.frontColor._1, PlayerFrame.frontColor._2, PlayerFrame.frontColor._3
      )
    }
  }

  registerEvent(GameEvents.onActionTaken)((_: Frame, gameState: GameState, _: GameAction) => {
    playerPoints.setText(gameState.points(player).toString)
    setTrickText(gameState)
    try {
      colorFocus(player == gameState.turnOfPlayer._1)
    } catch {
      case _: Throwable =>
    }

    if (playerClient.playerName != player) {
      if (gameState.hands.isDefinedAt(player) && gameState.hands(player).nonEmpty) {
        handFrame.show()
        handFrame.updateCardPlaces(player)
      } else {
        handFrame.hide()
      }
    }
  })


  val handFrame: HandFrame = new HandFrame(player, playerClient, frameWidth = 70.0)
  handFrame.removeScript(ScriptKind.OnClick)
  handFrame.setParent(this)

  handFrame.clearAllPoints()
  handFrame.setPoint(TopRight, this, BottomRight, 0, -10)

  handFrame.hide()




}


object PlayerFrame {
  private val backgroundColor: (Double, Double, Double) = (0.85, 0.85, 0.85)
  private val frontColor: (Double, Double, Double) = (70 / 255.0, 130 / 255.0, 1.0)
  private val focusedColor: (Double, Double, Double) = (1.0, 165 / 255.0, 0.0)
}