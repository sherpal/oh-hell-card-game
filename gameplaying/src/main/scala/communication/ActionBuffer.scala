package communication

import gamelogic.GameAction

import scala.collection.mutable

/**
 * An ActionBuffer stores incoming messages, and allow you to flush when you want to treat all of them.
 */
class ActionBuffer {

  private val messageQueue: mutable.Queue[GameAction] = mutable.Queue()

  def enqueue(action: GameAction): Unit = messageQueue.enqueue(action)

  def apply(action: GameAction): Unit = enqueue(action)

  def flush(actionHandler: GameAction => Unit): Unit =
    while (messageQueue.nonEmpty) actionHandler.apply(messageQueue.dequeue())

  def dequeue(actionHandler: GameAction => Unit): Unit = actionHandler.apply(messageQueue.dequeue())

}
