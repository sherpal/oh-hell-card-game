package exceptions

/**
 * Raised when trying to access the Game of a GameCreation object if the game is not launched yet.
 */
case class GameNotLaunchedException(s: String) extends Exception
