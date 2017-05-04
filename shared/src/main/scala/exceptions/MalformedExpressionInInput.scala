package exceptions

/**
 * Raised in the menus if some input filled is malformed.
 */
case class MalformedExpressionInInput(msg: String) extends Exception
