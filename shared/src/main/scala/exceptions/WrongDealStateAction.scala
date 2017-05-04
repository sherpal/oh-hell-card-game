package exceptions

/**
 * This Exception is raised if you try to make an action in a Deal which does not correspond to its DealState.
 */
case class WrongDealStateAction(msg: String) extends Exception
