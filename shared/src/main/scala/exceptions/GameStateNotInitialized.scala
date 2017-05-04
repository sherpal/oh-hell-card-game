package exceptions

/**
 * Raised when trying to access a GameState that is not yet set
 */
case class GameStateNotInitialized(msg: String) extends Exception