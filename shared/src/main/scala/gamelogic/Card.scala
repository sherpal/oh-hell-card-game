package gamelogic

import scala.collection.mutable


case class Card(value: CardValue, color: CardColor) {
  def beat(that: Card, trump: CardColor, startingColor: CardColor): Boolean = that match {
    case Card(v, c) if color == trump && c == trump => value > v
    case Card(_, _) if color == trump => true
    case Card(_, c) if c == trump => false
    case Card(v, c) if color == startingColor && c == startingColor => value > v
    case Card(_, _) if color == startingColor => true
    case Card(_, c) if c == startingColor => false
    case _ => false
  }

  override def toString: String = value.toString + " of " + color.toString

}

class DefaultCard(value: DefaultCardValue, color: CardColor) extends Card(value, color) {
  val toCardImageName: String = value.makeCardImageName(color)
}

object DefaultCard {
  def apply(value: DefaultCardValue, color: CardColor): DefaultCard = new DefaultCard(value, color)
}


object Card {
  def defaultDeck: List[DefaultCard] = for
    {
      color <- List(Spade, Diamond, Club, Heart)
      value <- List(Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King, Ace)
    } yield DefaultCard(value, color)

  def shuffledDefaultDeck: List[Card] = scala.util.Random.shuffle(defaultDeck)

  def shuffle(deck: Seq[Card]): mutable.Queue[Card] = {
    val q = mutable.Queue[Card]()
    scala.util.Random.shuffle(deck).foreach(card => q.enqueue(card))
    q
  }

  def handWinner(cards: Iterable[Card], trump: CardColor, startingColor: CardColor): Card =
  cards.foldLeft(cards.head)((c1: Card, c2: Card) => if (c1.beat(c2, trump, startingColor)) c1 else c2)
}
