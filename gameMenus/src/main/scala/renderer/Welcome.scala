package renderer

import globalvariables.VariableStorage
import org.scalajs.dom


/**
 * Manages what happens in the main screen html file.
 */
object Welcome {


  if (VariableStorage.retrieveValue("gameCanceled") != null) {
    dom.window.alert(VariableStorage.retrieveValue("gameCanceled").asInstanceOf[String])
    VariableStorage.unStoreValue("gameCanceled")
  }

}
