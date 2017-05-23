package guiobjects

import communication.{ActionBuffer, PlayerClient}
import complex.Complex
import gameengine.Engine
import gamelogic.{Card, GameAction, GameEvents, PlayCard}
import graphics.CardGraphics
import gui._

import scala.scalajs.js.timers.setTimeout

/**
 * PlayTable will represent the table on which players are playing.
 *
 * It helps players find their seats.
 */
class PlayTable(playersInOrder: Vector[PlayerFrame], handFrame: HandFrame, playerClient: PlayerClient) {

  val xRadius: Double = 300 - 15
  val yRadius: Double = 200
  val center: Complex = Complex(0, 50)

  private def indexToAngle(j: Int): Double = - math.Pi / 2 - j * 2 * math.Pi / players.length


  val players: Vector[PlayerFrame] = {
    val handFramePlayerIndex = playersInOrder.find(_.player == handFrame.player) match {
      case Some(playerFrame) => playersInOrder.indexOf(playerFrame)
      case None => 0 // this should never happen, as the client player should always be playing.
    }

    playersInOrder.drop(handFramePlayerIndex) ++ playersInOrder.take(handFramePlayerIndex)
  }

  private def playerAngle(player: PlayerFrame): Double = indexToAngle(players.indexOf(player))

  private def placePlayers(): Unit = {
    def angleToAnchor(angle: Double): (Point, Complex) = (math.sin(angle) > 0, math.cos(angle) > 0) match {
      case (true, true) => // First quadrant, anchoring to BottomLeft
        (BottomLeft, center + Complex(xRadius * math.cos(angle), yRadius * math.sin(angle)))
      case (true, false) => // Second quadrant, anchoring to BottomRight
        (BottomRight, center + Complex(xRadius * math.cos(angle), yRadius * math.sin(angle)))
      case (false, false) => // Third quadrant, anchoring to TopRight
        (TopRight, center + Complex(xRadius * math.cos(angle), yRadius * math.sin(angle)))
      case (false, true) => // Fourth quadrant, anchoring to TopLeft
        (TopLeft, center + Complex(xRadius * math.cos(angle), yRadius * math.sin(angle)))
    }

    players.zipWithIndex.foreach({case (frame, idx) =>
      val angle = indexToAngle(idx)
      val (point, position) = angleToAnchor(angle)
      frame.clearAllPoints()
      frame.setPoint(point, UIParent, Center, position.re, position.im)
    })
  }
  placePlayers()

  private val allCards: List[CardGraphics] = Card.defaultDeck.map(new CardGraphics(_))

  private var cardPlayed: List[(CardGraphics, PlayerFrame)] = Nil

//  private var treatingMessage: Boolean = true

  private def addCard(player: String, card: Card): Unit = {
    cardPlayed :+= (allCards.find(_.card == card).get, playersInOrder.find(_.player == player).get)
    cardPlayed = placePlayedCards(cardPlayed)
//    if (cardPlayed.length == playersInOrder.length) {
//      treatingMessage = false
//      setTimeout(2000) {
//        treatingMessage = true
//        cardPlayed = Nil
//        buffer.flush(actionHandler)
//      }
//    }
  }

  private def placePlayedCards(cardsToPlace: List[(CardGraphics, PlayerFrame)]): List[(CardGraphics, PlayerFrame)] = {
    cardsToPlace
      .foreach({ case (card, frame) =>
        val angle = playerAngle(frame)
        val xR = xRadius / 5
        val yR = yRadius / 5
        card.setCenter(
          center + Complex(xR * math.cos(angle), yR * math.sin(angle))
        )
      })
    cardsToPlace
  }

  private val buffer: ActionBuffer = new ActionBuffer
  private def actionHandler(action: GameAction): Unit = action match {
    case action: PlayCard => addCard(action.player, action.card)
    case _ =>
  }

  private val helperFrame: Frame = new Frame()
  helperFrame.registerEvent(GameEvents.onPlayerPlaysCard)((_: Frame, player: String, card: Card) => {
    buffer.enqueue(PlayCard(player, card))
//    if (treatingMessage) {
      buffer.flush(actionHandler)
//    }
  })
  helperFrame.registerEvent(GameEvents.onNewHand)((_: Frame) => {
    cardPlayed = Nil
  })
  helperFrame.registerEvent(GameEvents.onNewDeal)((_: Frame, _: Int, _: Int, _: Int, _: Int) => {
    cardPlayed = Nil
  })


  def draw(): Unit = {
    Engine.painter.withColor(0,0,0) { //TODO choose appropriate color
      Engine.painter.drawEllipse(center, xRadius, yRadius, segments = 200)
    }

    placePlayedCards(cardPlayed).foreach(_._1.drawCard())
  }

  def cardUnderMouse(x: Double, y: Double): Option[CardGraphics] = cardPlayed.map(_._1)
    .reverse
    .find(_.isMouseOver(x, y))


}
