package gamelogic

import exceptions.NotDefaultValue


case class CardValue(value: Int) extends Ordered[CardValue] {

  def compare(that: CardValue): Int = this.value.compare(that.value)

}

abstract sealed class DefaultCardValue(value: Int) extends CardValue(value) {
  val stringValue: String

  def makeCardImageName(color: CardColor): String =
    s"${stringValue}_of_${color.toString.toLowerCase}s.png"

}

object DefaultCardValue {
  def apply(value: Int): DefaultCardValue = value match {
    case 2 => Two
    case 3 => Three
    case 4 => Four
    case 5 => Five
    case 6 => Six
    case 7 => Seven
    case 8 => Eight
    case 9 => Nine
    case 10 => Ten
    case 11 => Jack
    case 12 => Queen
    case 13 => King
    case 14 => Ace
    case _ => throw NotDefaultValue(s"$value is not a DefaultCardValue")

  }
}

object Two extends DefaultCardValue(2) {
  val stringValue: String = "2"
}
object Three extends DefaultCardValue(3) {
  val stringValue: String = "3"
}
object Four extends DefaultCardValue(4) {
  val stringValue: String = "4"
}
object Five extends DefaultCardValue(5) {
  val stringValue: String = "5"
}
object Six extends DefaultCardValue(6) {
  val stringValue: String = "6"
}
object Seven extends DefaultCardValue(7) {
  val stringValue: String = "7"
}
object Eight extends DefaultCardValue(8) {
  val stringValue: String = "8"
}
object Nine extends DefaultCardValue(9) {
  val stringValue: String = "9"
}
object Ten extends DefaultCardValue(10) {
  val stringValue: String = "10"
}
object Jack extends DefaultCardValue(11) {
  val stringValue: String = "jack"
}
object Queen extends DefaultCardValue(12) {
  val stringValue: String = "queen"
}
object King extends DefaultCardValue(13) {
  val stringValue: String = "king"
}
object Ace extends DefaultCardValue(14) {
  val stringValue: String = "ace"
}