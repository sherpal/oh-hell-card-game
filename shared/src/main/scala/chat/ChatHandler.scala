package chat

import networkcom.{ChatMessageType, Client}
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.KeyboardEvent

import scala.collection.mutable
import scala.scalajs.js


class ChatHandler[MessageType <: ChatMessageType](playerName: String,
                                                  input: html.Input,
                                                  container: html.Div,
                                                  client: Client,
                                                  fac: (String) => MessageType) {

  private case class Color(red: Int, green: Int, blue: Int) {
    def toHex: String = "#%02X%02X%02X".format(red, green, blue)
  }

  private val definedColors: List[Color] = List(
    Color(255, 0, 0),
    Color(0, 0, 255),
    Color(0, 100, 0),
    Color(204, 204, 0),
    Color(204, 0, 204),
    Color(102, 205, 170),
    Color(255, 165, 0)
  )

  private val playerColors: mutable.Map[String, Color] = mutable.Map()

  input.onkeyup = (event: KeyboardEvent) => {
    event.preventDefault()
    if (event.key == "Enter" && input.value.trim != "") {
      client.sendReliable(fac(input.value))
      input.value = ""
    }
  }

  def receivedChatMessage(msg: MessageType): Unit = {
    val before = allChatMessages.find(_.time < msg.time)
    if (before.nonEmpty && before.get.sender == msg.sender) {
      before.get.addText(msg.s)
    } else {
      new ChatMessageElement(msg.s, msg.sender, msg.time)
    }

    container.scrollTop = container.scrollHeight - container.clientHeight
  }

  private var allChatMessages: List[ChatMessageElement] = Nil


  private class ChatMessageElement(msg: String, val sender: String, val time: Long) {
    if (!playerColors.isDefinedAt(sender)) {
      playerColors += (sender -> definedColors(playerColors.size % definedColors.length))
    }

    val element: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
    element.className = "chatElement"
    element.style.border = "2px solid " + playerColors(sender).toHex

    if (sender == playerName) {
      element.style.asInstanceOf[js.Dynamic].`align-items` = "flex-end"
    }

    val senderElement: html.Paragraph = dom.document.createElement("p").asInstanceOf[html.Paragraph]
    //senderElement.style.color = playerColors(sender).toHex
    senderElement.style.height = "12px"
    senderElement.style.fontWeight = "bold"
    senderElement.innerHTML = sender
    element.appendChild(senderElement)


    var textElements: List[html.Paragraph] = Nil

    def addText(content: String): Unit = {
      textElements = dom.document.createElement("p").asInstanceOf[html.Paragraph] +: textElements
      textElements.head.textContent = content
      textElements.head.style.textAlign = "justify"
      textElements.head.style.width = "100%"
      textElements.head.style.wordWrap = "break-word"
      element.appendChild(textElements.head)
    }

    addText(msg)

    // we take messages until we find a message that was received more than 10 seconds earlier
    // (there will most likely not have 10s of delay, and even then, we would not really care any more about
    // sorting them correctly)
    val (after, before) = allChatMessages.takeWhile(_.time > time - 10000).partition(_.time > time)
    after.foreach(e => container.removeChild(e.element))
    allChatMessages = (after.sortBy(_.time) :+ this) ++ before
    container.appendChild(element)
    after.foreach(e => container.appendChild(e.element))
  }

}
