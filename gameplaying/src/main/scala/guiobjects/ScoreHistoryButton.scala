package guiobjects

import communication.PlayerClient
import gui._

/**
 * Opens the Score History sheet when clicking on it.
 */
class ScoreHistoryButton(playerClient: PlayerClient, anchorFrame: Frame) extends Button(UIParent) {

  setSize(anchorFrame.width, 30)
  setText("Scores")
  setTextColor(1,1,1)
  setPoint(TopRight, anchorFrame, BottomRight, 0, -10)

  private val bg = createTexture()
  bg.setAllPoints()
  bg.setVertexColor(0,0,0)

  private val border = createTexture(layer = Highlight)
  border.setMode(LineMode)
  border.lineWidth = 2
  border.setAllPoints()
  border.setVertexColor(1,1,1)


  setScript(ScriptKind.OnClick)((_: Frame, _: Double, _: Double, button: Int) => if (button == 0) {
    playerClient.showScoreHistoryWindow()
    hide()
  })


}
