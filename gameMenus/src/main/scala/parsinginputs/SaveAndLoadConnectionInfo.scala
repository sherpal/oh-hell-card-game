package parsinginputs

import io.{ConnectionToGameInfo, IO}
import org.scalajs.dom
import org.scalajs.dom.html

import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Can save and load connection info
 */
object SaveAndLoadConnectionInfo {

  def load(host: Boolean): Unit = {
    val fileName = if (host) "connectionInfoHost.sav" else "connectionInfoJoin.sav"
    IO.open(s"/saved/$fileName").onComplete({
      case Success(fd) =>
        IO.close(fd)
        val recordedInfo = IO.readFileContent(s"/saved/$fileName")
        recordedInfo.onComplete({
          case Success(content) =>
            content match {
              case ConnectionToGameInfo(pseudo, gameName, address, port) =>
                dom.document.getElementById("inputName").asInstanceOf[html.Input].value = pseudo
                dom.document.getElementById("inputGameName").asInstanceOf[html.Input].value = gameName
                dom.document.getElementById("inputAddress").asInstanceOf[html.Input].value = address
                dom.document.getElementById("inputPort").asInstanceOf[html.Input].value = port.toString
              case _ =>
            }
          case _ =>
        })
      case _ =>
    })
  }

  def save(host: Boolean, pseudo: String, gameName: String, address: String, port: Int): Unit = {
    val fileName = if (host) "connectionInfoHost.sav" else "connectionInfoJoin.sav"
    IO.mkdir("saved").andThen({
      case _ =>
        IO.writeFileContent(
          s"/saved/$fileName", ConnectionToGameInfo(pseudo, gameName, address, port),
          (_) => if (scala.scalajs.LinkingInfo.developmentMode) println("could not save connection data :/")
        )
    })

  }

}
