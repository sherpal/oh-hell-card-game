package guiobjects

import gamelogic.{Card, GameEvents}
import graphics.CardGraphics

import gui._

/**
 * A small Frame put at the BottomLeft of the screen that displays the Trump for the Deal.
 */
object TrumpFrame extends Frame(UIParent) {

  private val cardWidth: Double = 100

  setPoint(BottomLeft)
  setWidth(cardWidth * 1.1)
  setHeight(width / 500 * 726)

  private val border = createTexture()
  border.setAllPoints()
  border.setVertexColor(198 / 255.0, 195 / 255.0, 214 / 255.0)
  border.setMode(LineMode)
  border.lineWidth = 5

  private val title = createFontString()
  title.setPoint(Bottom, this, Top)
  title.setWidth(width)
  title.setHeight(25)
  title.setText("Trump:")
  title.setTextColor(198 / 255.0, 195 / 255.0, 214 / 255.0)
  title.setJustifyH(JustifyLeft)

  private val cardImage = createTexture()
  cardImage.setPoint(Center)
  cardImage.setSize(cardWidth, cardWidth / 500 * 726)

  private var cardGraphics: Option[CardGraphics] = None

  def setTrumpCard(card: Card): Unit = {
    cardGraphics = Some(new CardGraphics(card))
    cardImage.setTexture(Some(cardGraphics.get.image))
  }

  registerEvent(GameEvents.onChooseTrump)((_: Frame, card: Card) => {setTrumpCard(card)})
}
