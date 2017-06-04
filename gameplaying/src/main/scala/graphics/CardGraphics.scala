package graphics

import complex.Complex
import gameengine.Engine
import gamelogic.{Card, DefaultCard}
import org.scalajs.dom
import org.scalajs.dom.{Event, html}

/**
 * A CardGraphics represents a card in the hand of the player.
 */
case class CardGraphics(card: Card) {

  private var _width: Double = CardGraphics.cardWidth
  def width: Double = _width
  def setWidth(w: Double): Unit = {
    _width = w
  }

  private var _height: Double = CardGraphics.cardHeight
  def height: Double = _height
  def setHeight(h: Double): Unit = {
    _height = h
  }

  private var _center: Complex = 0
  def center: Complex = _center
  def setCenter(z: Complex): Unit = {
    _center = z
  }
  def setCenter(x: Double, y: Double): Unit = setCenter(Complex(x, y))

  def topLeft: Complex = _center + Complex(- width / 2, height / 2)


  private var imageIsLoaded: Boolean = false
  val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
  card match {
    case default: DefaultCard =>
      image.src = "../../card_images/" + default.toCardImageName
      image.onload = (_: Event) =>
        imageIsLoaded = true
    case _ =>
      println("Only [[DefaultCard]]s are supported for now.")
  }

  def isMouseOver(z: Complex): Boolean =
    z.re > topLeft.re && z.re < topLeft.re + width && z.im < topLeft.im && z.im > topLeft.im - height

  def isMouseOver(x: Double, y: Double): Boolean = isMouseOver(Complex(x, y))

  def drawCard(): Unit = if (imageIsLoaded) {
    Engine.painter.drawImage(image, topLeft, width, height)
  }

}


object CardGraphics {
  val cardWidth: Double = 60
  val cardHeight: Double = cardWidth / 500 * 726

  private val defaultCards: List[CardGraphics] = Card.defaultDeck.map(CardGraphics(_))

  def cardsInHand(hand: List[Card]): List[CardGraphics] =
    defaultCards.filter(cardGraphics => hand.contains(cardGraphics.card))

  def drawHand(cards: List[CardGraphics]): Unit = {
    cards.foreach(_.drawCard())
  }

  def cardUnderMouse(z: Complex, hand: List[Card]): Option[CardGraphics] =
    cardsInHand(hand).foldLeft(None: Option[CardGraphics])((b, card) => if (card.isMouseOver(z)) Some(card) else b)

  def cardUnderMouse(x: Double, y: Double, hand: List[Card]): Option[CardGraphics] =
    cardUnderMouse(Complex(x, y), hand)

}