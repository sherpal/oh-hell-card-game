package renderer

import globalvariables.VariableStorage
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.{KeyboardEvent, MouseEvent}
import sharednodejsapis.{BrowserWindow, IPCMainEvent, IPCRenderer}

import scala.scalajs.js

/**
 * Manages what happens in the Bet window.
 */
object BetWindow {

  var nbrOfCardsDistributed: Int = 0
  var possibleBets: List[Int] = Nil

  val inputBets: html.Input = dom.document.getElementById("inputBets").asInstanceOf[html.Input]
  inputBets.onkeyup = (event: KeyboardEvent) => {
    event.preventDefault()
    if (event.key == "Enter") {
      sendBet()
    }
  }

  val okButton: html.Anchor = dom.document.getElementById("confirm").asInstanceOf[html.Anchor]
  okButton.onclick = (_: MouseEvent) => {
    sendBet()
  }

  def thisWindow: BrowserWindow = BrowserWindow.getFocusedWindow()


  private def sendBet(): Unit = {
    try {
      val value = inputBets.valueAsNumber
      if (possibleBets.contains(value)) {
        thisWindow.getParentWindow().webContents.send("bet-number-chosen", value)
        inputBets.value = ""
        thisWindow.close()
      } else {
        dom.window.alert("Possible values are:\n" + possibleBets.mkString("\n"))
      }
    } catch {
      case _: Throwable =>
    }
  }

  // should be useless now that I use getParentWindow method
  private var windowId: Int = 0

  IPCRenderer.on("choose-bets", (_: IPCMainEvent, id: Int) => {
    windowId = id

    nbrOfCardsDistributed = VariableStorage.retrieveGlobalValue("nbrOfCardsDistributed").asInstanceOf[Int]
    possibleBets = VariableStorage.retrieveGlobalValue("possibleBets")
      .asInstanceOf[js.Array[Int]].toList

    val notAllowedBet = (0 to nbrOfCardsDistributed).indexWhere(!possibleBets.contains(_))
    if (notAllowedBet >= 0) {
      dom.document.getElementById("instructions").asInstanceOf[html.Paragraph].innerHTML =
        s"Insert number of bets (0 to $nbrOfCardsDistributed, can't be $notAllowedBet)"
    } else {
      dom.document.getElementById("instructions").asInstanceOf[html.Paragraph].innerHTML =
        s"Insert number of bets (0 to $nbrOfCardsDistributed)"
    }
  })

}
