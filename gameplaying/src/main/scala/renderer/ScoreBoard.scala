package renderer

import globalvariables.VariableStorage
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.{BeforeUnloadEvent, Event}
import sharednodejsapis.{BrowserWindow, BrowserWindowOptions, Path}

import scala.scalajs.js

object ScoreBoard {

  try {
    val tBody = dom.document.getElementById("scoreBoard").asInstanceOf[html.TableDataCell]

    val (players, points) = VariableStorage.retrieveValue("endGamePoints").asInstanceOf[js.Array[String]]
      .toList
      .zipWithIndex
      .partition(_._2 % 2 == 0)
    val playerPoints = players.map(_._1).zip(points.map(_._1.toInt)).sortBy(_._2).reverse
    val rows = playerPoints.zipWithIndex.map({case ((playerName, pointsOfPlayer), idx) =>
        val tr = dom.document.createElement("tr").asInstanceOf[html.TableRow]
        val positionTd = dom.document.createElement("td").asInstanceOf[html.TableCol]
        positionTd.innerHTML = (idx + 1).toString
        val nameTd = dom.document.createElement("td").asInstanceOf[html.TableCol]
        nameTd.innerHTML = playerName
        val pointsTd = dom.document.createElement("td").asInstanceOf[html.TableCol]
        pointsTd.innerHTML = pointsOfPlayer.toString
        tr.appendChild(positionTd)
        tr.appendChild(nameTd)
        tr.appendChild(pointsTd)
        tBody.appendChild(tr)
        tr
    })
    rows.head.className = "success"

  } catch {
    case _: Throwable =>
      dom.window.alert("FATAL ERROR: did not store the leader board, sorry.")
      dom.window.location.href = "../../gamemenus/mainscreen/mainscreen.html"
  }

  val scoreHistoryButton: html.Anchor = dom.document.getElementById("scoreHistory").asInstanceOf[html.Anchor]

  scoreHistoryButton.onclick = (_) => showScoreHistoryWindow()

  var scoreHistoryWindow: Option[BrowserWindow] = None

  private val scoreHistoryCloseHandler: js.Function1[Event, Unit] = (_: Event) => {
    try {
      scoreHistoryWindow = None
    } catch {
      case _: Throwable =>
    }
  }

  private def removeCloseHistoryHandler(): Unit = scoreHistoryWindow match {
    case Some(window) => window.removeAllListeners("close")
    case None =>
  }

  def showScoreHistoryWindow(): Unit = if (scoreHistoryWindow.isEmpty) {
    // adding an event on the main browser window to remove the previous event.
    // if we don't do that, and the main window gets close before the score board, it creates an error in the main
    // process, which we absolutely don't want.
    dom.window.onbeforeunload = (_: BeforeUnloadEvent) => {
      removeCloseHistoryHandler()
    }

    scoreHistoryWindow = Some(new BrowserWindow(new BrowserWindowOptions {}))

    scoreHistoryWindow.get.setMenu(null)

    scoreHistoryWindow.get.loadURL("file://" +
      Path.join(js.Dynamic.global.selectDynamic("__dirname").asInstanceOf[String], "./scorehistory.html")
    )

    scoreHistoryWindow.get.on("close", scoreHistoryCloseHandler)

    if (scala.scalajs.LinkingInfo.developmentMode)
      scoreHistoryWindow.get.webContents.openDevTools()

    scoreHistoryWindow.get.webContents.on("did-finish-load", () =>
      scoreHistoryWindow.get.webContents.send("send-info", VariableStorage.retrieveValue("fullScoreHistory"))
    )
  } else {
    scoreHistoryWindow.get.focus()
  }


}
