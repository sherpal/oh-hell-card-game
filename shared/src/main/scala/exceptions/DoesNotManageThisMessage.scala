package exceptions

/**
 * Raised when you fall into a message callback that does not handle this callback.
 * This should really never happen!
 */
case class DoesNotManageThisMessage(msg: String) extends Exception
