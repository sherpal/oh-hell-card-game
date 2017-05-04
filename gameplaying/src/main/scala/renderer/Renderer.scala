package renderer

import communication.PlayerClient
import gamelogic.GameState

import scala.scalajs.js
import scala.scalajs.js.JSApp
import globalvariables.VariableStorage.retrieveValue
import gui.UIParent
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.Event

object Renderer extends JSApp {
  def main(): Unit = {

    var playerClient: Option[PlayerClient] = None

    dom.document.title match {
      case "Oh Hell!" =>
        try {
          val playerName: String = retrieveValue("playerName").asInstanceOf[String]
          val gameName: String = retrieveValue("gameName").asInstanceOf[String]
          val address: String = retrieveValue("address").asInstanceOf[String]
          val port: Int = retrieveValue("port").asInstanceOf[Int]
          val password: Int = retrieveValue("password").asInstanceOf[Int]
          val players: Vector[String] = retrieveValue("players").asInstanceOf[js.Array[String]].toVector
          val maxNbrOfCards: Int = retrieveValue("maxNbrCards").asInstanceOf[Int]

          playerClient = Some(new PlayerClient(
            playerName, gameName, address, port, password, GameState.originalState(players, maxNbrOfCards)
          ))


          val canvas: html.Canvas = dom.document.getElementById("canvas").asInstanceOf[html.Canvas]
          dom.window.addEventListener("resize", (_: Event) => {
            canvas.width = dom.window.innerWidth.toInt - 10
            canvas.height = dom.window.innerHeight.toInt - 10
            UIParent.resize()
          })
        } catch {
          case e: Throwable =>
            e.printStackTrace()
            dom.window.alert("FATAL ERROR: something went wrong with connection data storage. Please try again.")
            dom.window.location.href = "../../gamemenus/mainscreen/mainscreen.html"
        }
      case "Bets" =>
        BetWindow
      case "Score Board" =>
        ScoreBoard
      case _ =>
        println("I should not be here.")
        dom.window.alert("FATAL ERROR: not a correct Html file loading this.")
        dom.window.location.href = "../../gamemenus/mainscreen/mainscreen.html"
    }
  }
}
