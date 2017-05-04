package exceptions

/**
 * Raised when trying to create DefaultCardValue with number different from 2 to 14.
 */
case class NotDefaultValue(msg: String) extends Exception