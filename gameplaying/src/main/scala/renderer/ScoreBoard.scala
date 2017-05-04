package renderer

import globalvariables.VariableStorage
import org.scalajs.dom
import org.scalajs.dom.html

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



}
