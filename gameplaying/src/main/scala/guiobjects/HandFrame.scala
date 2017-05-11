package guiobjects

import communication.PlayerClient
import complex.Complex
import gameengine.Engine
import gamelogic.{Card, GameAction, GameEvents, GameState}
import graphics.CardGraphics
import gui._


/**
 * We'll display cards in hand at the bottom of the square, in this Frame.
 * @param player The Player that corresponds to this client.
 */
class HandFrame(val player: String, playerClient: PlayerClient, frameWidth: Double = 600) extends Frame(UIParent) {

  val cardWidth: Double = math.max(frameWidth / 10, 60)
  val cardHeight: Double = cardWidth / 500 * 726

  setPoint(Bottom)
  setSize(frameWidth, cardHeight * 1.1)

  private val border = createTexture()
  border.setAllPoints()
  border.setMode(LineMode)
  border.lineWidth = 5
  border.setVertexColor(0,0,0)

  private val background = createTexture()
  background.setAllPoints()
  background.setVertexColor(198 / 255.0, 195 / 255.0, 214 / 255.0)


  def hand: List[Card] = playerClient.currentGameState.hands.get(player) match {
    case Some(cards) => cards.toList
    case _ => Nil
  }

  private var lastHand: List[Card] = hand

  private val allCards: List[CardGraphics] = Card.defaultDeck.map(new CardGraphics(_))

  private def cards: List[CardGraphics] = allCards.filter(card => lastHand.contains(card.card))

  private def placeCards(cardsToPlace: List[CardGraphics]): Unit = {
    allCards.foreach(card => {
      card.setWidth(cardWidth)
      card.setHeight(cardHeight)
    })
    val nbrOfCards = cardsToPlace.length
    val spaceBetweenCards = if (nbrOfCards * cardWidth < this.width)
      cardWidth
    else
      (this.width - cardWidth) / (nbrOfCards - 1)

    val startingAbscissa = this.center._1 - spaceBetweenCards / 2 * (nbrOfCards - 1)
    cardsToPlace.zipWithIndex.foreach({
      case (card, idx) => card.setCenter(startingAbscissa + idx * spaceBetweenCards, this.center._2)
    })
  }

  private def drawCardsInHand(): Unit = cards.foreach(_.drawCard())

  private var isPlayerTurn: Boolean = false

  override def drawChildren(): Unit = {
    super.drawChildren()

    drawCardsInHand()

    if (!isPlayerTurn && player == playerClient.playerName) {
      Engine.painter.withColor(0,0,0,0.5) {
        Engine.painter.drawRectangle(Complex(left, top), width, height)
      }
    }
  }

  def cardUnderMouse(x: Double, y: Double): Option[CardGraphics] = allCards
    .filter(card => lastHand.contains(card.card))
    .foldLeft(None: Option[CardGraphics])(
    (acc: Option[CardGraphics], card: CardGraphics) => if (card.isMouseOver(x, y)) Some(card) else acc
  )

  def updateCardPlaces(p: String): Unit = {
    if (player == p) {
      lastHand = hand
      placeCards(cards)
    }
  }

  private val decoyCard: Button = new Button(this)
  decoyCard.setPoint(Center)
  decoyCard.setSize(cardWidth, cardHeight)
  private val decoyBG: Texture = decoyCard.createTexture()
  decoyBG.setAllPoints()
  decoyBG.setTexture(Card.backImageSource)
  private val decoyVeil: Texture = decoyCard.createTexture(layer = Highlight)
  decoyVeil.setAllPoints()
  decoyVeil.setVertexColor(0,0,0,0.5)
  decoyCard.hide()

  decoyCard.setScript(ScriptKind.OnClick)((_: Frame, _: Double, _: Double, button: Int) => if (button == 0) {
    val c = playerClient.feelingLucky.center
    playerClient.feelingLucky.click(c._1, c._2, 0)
  })

  setScript(ScriptKind.OnClick)((_: Frame, x: Double, y: Double, button: Int) => {
    if (button == 0) {
      cardUnderMouse(x, y) match {
        case Some(card) =>
          playerClient.sendPlayCard(player, card.card)
        case None =>
      }
    }
  })

  setScript(ScriptKind.OnMouseMoved)((_: Frame, x: Double, y: Double, _: Double, _: Double, _: Int) => {
    playerClient.changeLastCardUnderMouse(x, y)
  })

  registerEvent(GameEvents.onPlayerPlaysCard)((_: Frame, p: String, _: Card) => {
    updateCardPlaces(p)
  })

  registerEvent(GameEvents.onPlayerReceivesHand)((_: Frame, p: String, _: List[Card]) => {
    updateCardPlaces(p)
  })

  registerEvent(GameEvents.onActionTaken)((_: Frame, state: GameState, _: GameAction) => {
    isPlayerTurn = state.turnOfPlayer._1 == player
    if (state.nbrCardsDistributed == 1 && player == playerClient.playerName &&
      !state.playedCards.keys.toSet.contains(player)) {
      decoyCard.show()
      if (isPlayerTurn) {
        decoyVeil.hide()
      } else {
        decoyVeil.show()
      }
    } else {
      decoyCard.hide()
    }
  })

  setScript(ScriptKind.OnUIParentResize)((_: Region) => {
    updateCardPlaces(player)
  })

}
