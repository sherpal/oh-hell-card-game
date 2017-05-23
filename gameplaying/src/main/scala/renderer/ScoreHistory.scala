package renderer

import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.{Event, MouseEvent}
import sharednodejsapis.IPCRenderer

/**
 * Manages what happens in the score history html file.
 */
object ScoreHistory {

  val table: html.Table = dom.document.getElementById("scoreTable").asInstanceOf[html.Table]

  IPCRenderer.on("send-info", (_: Event, tableContent: String) => {
    if (scala.scalajs.LinkingInfo.developmentMode)
      println(tableContent)

    table.innerHTML = tableContent
  })

  val button: html.Anchor = dom.document.getElementById("printToPdf").asInstanceOf[html.Anchor]

  val legend: html.Paragraph = dom.document.getElementById("legend").asInstanceOf[html.Paragraph]

  button.onclick = (_: MouseEvent) => {
    button.style.visibility = "hidden"
    legend.style.visibility = "hidden"
    IPCRenderer.send("print-to-pdf")
  }

  IPCRenderer.on("printed", (_: Event) => {
    button.style.visibility = "visible"
    legend.style.visibility = "visible"
  })

}
