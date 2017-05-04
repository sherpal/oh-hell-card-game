package exceptions


/**
 * This exception is raised when starting a Deal and wanting to distribute too many cards.
 */
case class TooMuchCardsToDistribute(msg: String) extends Exception
