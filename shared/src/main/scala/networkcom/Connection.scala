package networkcom

import java.util.Date

import boopickle.CompositePickler
import boopickle.Default._
import sharednodejsapis._

import scala.collection.mutable
import scala.scalajs.js.timers._
import scala.scalajs.js.typedarray.TypedArrayBuffer
import scala.scalajs.js.typedarray.TypedArrayBufferOps._


class Connection(peer: Peer, socket: Socket, callback: Message => Unit) {
  import Connection._

  private val latencyHistory: mutable.Queue[Int] = mutable.Queue[Int]()
  latencyHistory.enqueue(100)

  private var pingIntervalHandle: SetIntervalHandle = _

  private var connectedCheck: Boolean = false
  private var connected: Boolean = true

  private var nextOrderedId: Int = 0
  private var lastReceivedOrderedId: Int = -1

  private var nextReliableId: Int = 0
  private val sentReliableIds: mutable.Set[Int] = mutable.Set()
  private val receivedReliableIds: ReceivedIdSet = new ReceivedIdSet

  private var nextOrderedReliableId: Int = 0
  private val sentOrderedReliableIds: mutable.Set[Int] = mutable.Set()
  private val receivedOrderedReliableIds: ReceivedIdSet = new ReceivedIdSet
  private var queuedMessages: List[(Int, Message)] = List[(Int, Message)]()
  private var lastDeliveredOrderedReliable: Int = -1


  private val connectionCheckIntervalHandle: SetIntervalHandle = setInterval(10000) {
    if (connectedCheck) connectedCheck = false
    else {
      connected = false
      clearInterval(connectionCheckIntervalHandle)
    }
  }


  def activatePing(delay: Int = 1000): Unit = {
    deactivatePing()

    pingIntervalHandle = setInterval(delay) {
      ping()
    }
  }

  def deactivatePing(): Unit = {
    if (pingIntervalHandle != null) {
      clearInterval(pingIntervalHandle)
      pingIntervalHandle = null
    }
  }

  def onMessage(buffer: Buffer): Unit = {
    connectedCheck = true

    val msg = Unpickle[InternalMessage](InternalMessage.internalMessagePickler)
      .fromBytes(TypedArrayBuffer.wrap(buffer.buffer))


    msg match {
      case ReliableAck(id) => receivedReliableAck(id)
      case OrderedReliableAck(id) => receivedOrderedReliableAck(id)
      case Ping(time) => pong(time)
      case Pong(time, newTime) =>
        receivedPong(time)
        sendInternal(PongPong(newTime))
      case PongPong(time) => receivedPong(time)
      case Normal(actualMsg) => receivedNormalMessage(actualMsg)
      case Ordered(actualMsg, id) => receivedOrderedMessage(actualMsg, id)
      case Reliable(actualMsg, id) => receivedReliableMessage(actualMsg, id)
      case OrderedReliable(actualMsg, id) => receivedOrderedReliableMessage(actualMsg, id)
    }
  }


  def isConnected: Boolean = connected

  private def ping(): Unit =
    sendInternal(Ping(new Date().getTime))

  private def pong(time: Long): Unit =
    sendInternal(Pong(time, new Date().getTime))

  private def receivedPong(time: Long): Unit = {
    latencyHistory.enqueue((new Date().getTime - time).toInt / 2)
    if (latencyHistory.size > 10) latencyHistory.dequeue
  }


  def latency: Int = latencyHistory.max

  private def sendInternal(msg: InternalMessage): Unit = {
    implicit val pickler: CompositePickler[InternalMessage] = InternalMessage.internalMessagePickler
    val bb = Pickle.intoBytes(msg)
    val buffer = Buffer.from(bb.arrayBuffer())
    socket.send(buffer, peer.port, peer.address)
  }

  def sendNormal(msg: Message): Unit =
    sendInternal(Normal(msg))


  def sendReliable(msg: Message): Unit = {
    val id = nextReliableId
    nextReliableId += 1

    sentReliableIds += id

    def loop(): Unit = {
      sendInternal(Reliable(msg, id))


      setTimeout(4 * latency) {
        if (sentReliableIds.contains(id) && connected) loop()
      }
    }

    loop()
  }

  def sendOrderedReliable(msg: Message): Unit = {
    val id = nextOrderedReliableId
    nextOrderedReliableId += 1

    sentOrderedReliableIds += id

    def loop(): Unit = {
      sendInternal(OrderedReliable(msg, id))

      setTimeout(4 * latency) {
        if (sentOrderedReliableIds.contains(id) && connected) loop()
      }
    }

    loop()
  }


  private def receivedReliableAck(id: Int): Unit = {
    sentReliableIds -= id
  }

  private def receivedOrderedReliableAck(id: Int): Unit =
    sentOrderedReliableIds -= id


  def sendOrdered(msg: Message): Unit = {
    val id = nextOrderedId
    nextOrderedId += 1
    sendInternal(Ordered(msg, id))
  }


  private def receivedNormalMessage(msg: Message): Unit = callback(msg)

  private def receivedOrderedMessage(msg: Message, id: Int): Unit = {
    if (id > lastReceivedOrderedId) {
      lastReceivedOrderedId = id
      callback(msg)
    }
  }

  private def receivedReliableMessage(msg: Message, id: Int): Unit = {
    sendInternal(ReliableAck(id))

    // discarding already received messages
    if (!receivedReliableIds.contains(id)) {
      receivedReliableIds += id

      callback(msg)
    }
  }

  private def receivedOrderedReliableMessage(msg: Message, id: Int): Unit = {
    sendInternal(OrderedReliableAck(id))

    if (!receivedOrderedReliableIds.contains(id)) {
      receivedOrderedReliableIds += id

      if (id == lastDeliveredOrderedReliable + 1) {
        callback(msg)
        lastDeliveredOrderedReliable += 1
        service()
      } else {
        def insertMessage(id: Int, msg: Message, messageList: List[(Int, Message)]): List[(Int, Message)] = {
          if (messageList.isEmpty) List((id, msg))
          else if (messageList.head._1 > id) (id, msg) :: messageList
          else messageList.head :: insertMessage(id, msg, messageList.tail)
        }

        queuedMessages = insertMessage(id, msg, queuedMessages)
      }
    }
  }

  private def service(): Unit = {
    while (queuedMessages.nonEmpty && queuedMessages.head._1 == lastDeliveredOrderedReliable + 1) {
      lastDeliveredOrderedReliable += 1
      callback(queuedMessages.head._2)
      queuedMessages = queuedMessages.tail
    }
  }
}


object Connection {
  private class ReceivedIdSet {
    private var upToValue: Int = -1
    private val outOfOrder: mutable.Set[Int] = mutable.Set()

    def +=(id: Int): Unit = {
      if (id > upToValue) {
        if (id != upToValue + 1) {
          outOfOrder += id
        } else {
          upToValue = id
          while (outOfOrder.remove(upToValue + 1)) {
            upToValue += 1
          }
        }
      }
    }

    def contains(id: Int): Boolean = id <= upToValue || outOfOrder.contains(id)
  }

  private sealed abstract class InternalMessage
  private final case class ReliableAck(id: Int) extends InternalMessage
  private final case class OrderedReliableAck(id: Int) extends InternalMessage
  private final case class Ping(time: Long) extends InternalMessage
  private final case class Pong(time: Long, newTime: Long) extends InternalMessage
  private final case class PongPong(time: Long) extends InternalMessage
  private final case class Normal(msg: Message) extends InternalMessage
  private final case class Reliable(msg: Message, id: Int) extends InternalMessage
  private final case class Ordered(msg: Message, id: Int) extends InternalMessage
  private final case class OrderedReliable(msg: Message, id: Int) extends InternalMessage

  private object InternalMessage {
    implicit val internalMessagePickler: CompositePickler[InternalMessage] = {
      implicit val messagePickler: CompositePickler[Message] = Message.messagePickler
      compositePickler[InternalMessage]
        .addConcreteType[ReliableAck]
        .addConcreteType[OrderedReliableAck]
        .addConcreteType[Ping]
        .addConcreteType[Pong]
        .addConcreteType[PongPong]
        .addConcreteType[Normal]
        .addConcreteType[Reliable]
        .addConcreteType[Ordered]
        .addConcreteType[OrderedReliable]
    }
  }

}