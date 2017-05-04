package guiobjects

import communication.PlayerClient
import gui._
import networkcom.PlayRandomCard

/**
 * Plays a card randomly
 */
class RandomCardButton(handFrame: HandFrame, playerClient: PlayerClient) extends Button(handFrame) {
  setPoint(BottomRight, handFrame, TopRight)
  setSize(200,30)
  setText("I'm feeling lucky")

  hide()

  private val randomCardTex = createTexture()
  randomCardTex.setAllPoints()
  randomCardTex.setVertexColor(0,0,0)

  setScript(ScriptKind.OnClick)((_: Frame, _: Double, _: Double, button: Int) => {
    if (button == 0) {
      val state = playerClient.currentGameState
      if (!state.turnOfPlayer._2 && state.turnOfPlayer._1 == playerClient.playerName) {
        playerClient.sendReliable(PlayRandomCard(playerClient.gameName, playerClient.playerName))
      }
    }
  })

}
