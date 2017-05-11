package renderer

import gamelogic.Card
import graphics.CardGraphics
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.Event
import sharednodejsapis.IPCRenderer

/**
 * Display the last card under the mouse in big
 */
object CardViewer {

  val img: html.Image = dom.document.getElementById("card").asInstanceOf[html.Image]

  img.width = dom.window.innerWidth.toInt
  img.height = dom.window.innerHeight.toInt

  val allCards: List[CardGraphics] = Card.defaultDeck.map(new CardGraphics(_))

  IPCRenderer.on("change-card", (_: Event, value: Int, color: String) => {
    println(value, color)
    allCards.find(card => card.card.value.value == value && card.card.color.color == color) match {
      case Some(card) =>
        img.src = card.image.src
        img.width = dom.window.innerWidth.toInt
        img.height = dom.window.innerHeight.toInt
      case None =>
    }
  })

}
