package renderer

import globalvariables.VariableStorage
import org.scalajs.dom
import org.scalajs.dom.html

/**
 * Here we put all the codes that is common to all the menu pages.
 */
object Common {

  if (VariableStorage.retrieveValue("isThereServer").asInstanceOf[String] != null) {
    dom.document.getElementById("isThereServer").asInstanceOf[html.Paragraph].innerHTML =
      "Server port is " + VariableStorage.retrieveValue("isThereServer").asInstanceOf[String]
  }

}
