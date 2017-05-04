package exceptions

/**
 * Raised if a chosen port is less than 1024 or bigger than 65535
 */
case class IllegalPortChoice(msg: String) extends Exception
