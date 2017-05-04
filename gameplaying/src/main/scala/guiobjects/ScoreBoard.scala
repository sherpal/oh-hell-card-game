package guiobjects

import gui.{Tooltip, TopRight}

/**
 * ScoreBoard lists players from first to last in terms of points.
 */
object ScoreBoard extends Tooltip() {

  setPoint(TopRight)
  setSize(200, 10)

  private val background = createTexture()
  background.setAllPoints()
  background.setVertexColor(198 / 255.0, 195 / 255.0, 214 / 255.0)

  def setPlayerNames(points: Map[String, Int]): Unit = {
    setHeight(points.size * 25)
    clearLines()
    points.toList.sortBy(_._2).reverse.foreach({case (player, point) => addDoubleLine(
      player, point.toString,
      24 / 255.0, 77 / 255.0, 30 / 255.0, 20,
      24 / 255.0, 77 / 255.0, 30 / 255.0, 20
    )})
  }

}
