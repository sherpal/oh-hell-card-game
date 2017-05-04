package gamelogic


case class CardColor(color: String) {
  override def toString: String = color
}

object Spade extends CardColor("Spade")
object Heart extends CardColor("Heart")
object Diamond extends CardColor("Diamond")
object Club extends CardColor("Club")
