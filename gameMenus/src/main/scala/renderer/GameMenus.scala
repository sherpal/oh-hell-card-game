package renderer

import org.scalajs.dom
import org.scalajs.dom.html

import scala.scalajs.js.JSApp

/**
 * GameMenus will be launched in each html page of the menus.
 *
 * It will detect which page it is in, and do stuff accordingly.
 */
object GameMenus extends JSApp {
  def main(): Unit = {
    dom.document.getElementById("subtitle").asInstanceOf[html.Heading].innerHTML match {
      case "Welcome" =>
        if (scala.scalajs.LinkingInfo.developmentMode)
          println("We are in the main screen")

        Common
        Welcome

      case "Host Game" =>
        if (scala.scalajs.LinkingInfo.developmentMode)
          println("We are in Host Game")

        HostGame
        Common

      case "Join Game" =>
        if (scala.scalajs.LinkingInfo.developmentMode)
          println("We are in Join Game")

        JoinGame
        Common

      case "Create Server" =>
        if (scala.scalajs.LinkingInfo.developmentMode)
          println("We are in Create Server")

        CreateServer
        Common

      case "Host Game Settings" =>
        if (scala.scalajs.LinkingInfo.developmentMode)
          println("We are in Host Game Settings")

        GameSettingsCommon
        HostGameSettings

      case "Join Game Settings" =>
        if (scala.scalajs.LinkingInfo.developmentMode)
          println("We are in Join Game Settings")

        GameSettingsCommon
        JoinGameSettings

      case s =>
        dom.console.error(s"Did not recognize menu... h2 content is `$s`.")
    }
  }
}
