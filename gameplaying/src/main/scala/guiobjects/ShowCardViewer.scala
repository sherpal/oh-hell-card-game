package guiobjects

import communication.PlayerClient
import gui._

/**
 * A button designed to be clicked on to show the menu.
 */
class ShowCardViewer(playerClient: PlayerClient) extends Button(UIParent) {

  setPoint(TopLeft)
  setSize(150, 30)
  setText("Card Viewer")
  setTextColor(1,1,1)

  setScript(ScriptKind.OnClick)((_: Frame, _: Double, _: Double, button: Int) => if (button == 0) {
    playerClient.showCardViewer()
    hide()
  })

  private val bg = createTexture()
  bg.setAllPoints()
  bg.setVertexColor(0,0,0)

  private val border = createTexture(layer = Highlight)
  border.setMode(LineMode)
  border.lineWidth = 2
  border.setAllPoints()
  border.setVertexColor(1,1,1)


}
