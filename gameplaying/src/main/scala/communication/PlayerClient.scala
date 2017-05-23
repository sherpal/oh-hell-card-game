package communication

import chat.ChatHandler
import gameengine.{Engine, GameState => GameRunner}
import gamelogic.{PlayingCardState, _}
import globalvariables.VariableStorage
import graphics.CardGraphics
import gui._
import networkcom._
import org.scalajs.dom
import org.scalajs.dom.html
import guiobjects._
import org.scalajs.dom.raw.{BeforeUnloadEvent, Event}
import sharednodejsapis._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.timers.setTimeout



/**
 * This Client will communicate with the server during a game.
 */
class PlayerClient(val playerName: String,
                  val gameName: String,
                  val address: String,
                  val port: Int,
                  val password: Int,
                  val gameState: GameState) extends Client {

  connect()


  val canvas: html.Canvas = dom.document.getElementById("canvas").asInstanceOf[html.Canvas]
  val gameInterface: html.Div = dom.document.getElementById("gameInterface").asInstanceOf[html.Div]
  val chatContainer: html.Div = dom.document.getElementById("chat").asInstanceOf[html.Div]

  def updateSize(): Unit = {

    gameInterface.style.width = dom.window.innerWidth.toInt.toString + "px"
    gameInterface.style.height = dom.window.innerHeight.toInt.toString + "px"
    chatContainer.style.height = (dom.window.innerHeight.toInt - 10).toString + "px"
    canvas.width = dom.window.innerWidth.toInt - 300
    canvas.height = dom.window.innerHeight.toInt - 10
    UIParent.resize()

    // these funny three next lines come from
    // http://stackoverflow.com/questions/23474191/flexbox-height-not-updating-when-content-changes
    gameInterface.style.display = "none"
    gameInterface.offsetHeight
    gameInterface.style.display = "flex"
  }

  val updateSizeJS: js.Function1[Event, Unit] = (_: Event) => updateSize()

  dom.window.addEventListener("resize", updateSizeJS)
  BrowserWindow.fromId(VariableStorage.retrieveValue("windowId").asInstanceOf[Int]).on(
    "maximize", (_: Event) => {
      setTimeout(100) {
        updateSize()
      }
    }
  )
  BrowserWindow.fromId(VariableStorage.retrieveValue("windowId").asInstanceOf[Int]).on(
    "unmaximize", (_: Event) => {
      setTimeout(100) {
        updateSize()
      }
    }
  )


  private val chatHandler: ChatHandler[InGameChatMessage] = new ChatHandler[InGameChatMessage](
    playerName,
    dom.document.getElementById("chatInput").asInstanceOf[html.Input],
    dom.document.getElementById("chatMessageContainer").asInstanceOf[html.Div],
    this,
    InGameChatMessage(gameName, _, new java.util.Date().getTime, playerName)
  )

  private var gameActions: List[GameAction] = Nil

  private val handFrame: HandFrame = new HandFrame(playerName, this)

  val feelingLucky: RandomCardButton = new RandomCardButton(handFrame, this)

  private val playerFrames: Vector[PlayerFrame] = gameState.players.map(new PlayerFrame(_, this))
  private val trickViewer: LastTrickViewer = new LastTrickViewer(playerFrames.toSet)

  private val playTable: PlayTable = new PlayTable(playerFrames, handFrame, this)

  /**
   * Computes the current game state, given all the actions taken up to that point.
   *
   * Note: once computed, we could record a new gameState, but for a game like Rikiki, with so few actions, I don't
   * believe it is necessary. The number of actions at the end of the game should be of order a few hundreds.
   */
  def currentGameState: GameState = gameState(gameActions.drop(gameState.nbrOfPerformedActions))

  TrumpFrame
  if (scala.scalajs.LinkingInfo.developmentMode) GameLogs

  private def gameStarts(): Unit = {
    val gameInterface: html.Div = dom.document.getElementById("gameInterface").asInstanceOf[html.Div]
    gameInterface.style.visibility = "visible"
//    gameInterface.style.width = dom.window.innerWidth.toString
//    gameInterface.style.height = dom.window.innerHeight.toString
//    val canvas: html.Canvas = dom.document.getElementById("canvas").asInstanceOf[html.Canvas]
//    canvas.width = dom.window.innerWidth.toInt - 300
//    canvas.height = dom.window.innerHeight.toInt - 10
//    UIParent.resize()
    updateSize()

    dom.document.getElementById("waitingPlayersTitle").asInstanceOf[html.Heading].style.visibility = "hidden"
    dom.document.getElementById("waitingPlayersTitle").asInstanceOf[html.Heading].style.position = "absolute"
    dom.document.getElementById("waitingPlayers").asInstanceOf[html.Div].style.visibility = "hidden"
    dom.document.getElementById("waitingPlayers").asInstanceOf[html.Div].style.position = "absolute"

    Engine.startGameLoop()
  }


  IPCRenderer.on("bet-number-chosen", (_: IPCMainEvent, betNumber: Int) => {
    val action = BetTrickNumber(playerName, betNumber)
    if (currentGameState.legalActions(action)) {
      sendReliable(action.toMessage(gameName))
    } else { // should never happen as the check has to be done in the BetWindow.
      if (scala.scalajs.LinkingInfo.developmentMode)
        println(currentGameState.toString)

      showBetWindow(currentGameState)
    }
  })

  private def showBetWindow(gameState: GameState): Unit = {
    val betWindow: BrowserWindow = new BrowserWindow(new BrowserWindowOptions {
      override val width: js.UndefOr[Int] = 350
      override val height: js.UndefOr[Int] = 200

      override val frame: js.UndefOr[Boolean] = false

      override val parent: js.UndefOr[BrowserWindow] = BrowserWindow.fromId(
        VariableStorage.retrieveValue("windowId").asInstanceOf[Int]
      )

      override val modal: js.UndefOr[Boolean] = true
    })
    betWindow.loadURL("file://" +
      Path.join(js.Dynamic.global.selectDynamic("__dirname").asInstanceOf[String], "./bets.html")
    )

    val legalActions = gameState.legalActions
    val possibleBets = (0 to gameState.nbrCardsDistributed).filter(
      (j: Int) => legalActions(BetTrickNumber(playerName, j))
    ).toJSArray

    VariableStorage.storeGlobalValue("possibleBets", possibleBets)
    VariableStorage.storeGlobalValue("nbrOfCardsDistributed", gameState.nbrCardsDistributed)

    betWindow.webContents.on("did-finish-load", (_: Event) => {
      betWindow.webContents.send("choose-bets", VariableStorage.retrieveValue("windowId"))
    })
  }

  private var lastCardUnderMouse: Option[CardGraphics] = None

  private var cardViewer: Option[BrowserWindow] = None
  private val cardViewerWidth: Int = 300
  private val cardViewerHeight: Int = 300 * 726 / 500

  private val showCardViewerButton: ShowCardViewer = new ShowCardViewer(this)
  def showCardViewer(): Unit = {
    cardViewer = Some(new BrowserWindow(new BrowserWindowOptions {
      override val width: js.UndefOr[Int] = cardViewerWidth
      override val height: js.UndefOr[Int] = cardViewerHeight

      override val resizable: js.UndefOr[Boolean] = false
    }))

    cardViewer.get.setMenu(null)

    cardViewer.get.loadURL("file://" +
      Path.join(js.Dynamic.global.selectDynamic("__dirname").asInstanceOf[String], "./cardviewer.html")
    )

    cardViewer.get.on("close", (_: Event) => {
      cardViewer = None
      lastCardUnderMouse = None
      showCardViewerButton.show()
    })
  }

  private def sendCardToViewer(value: Int, color: String): Unit = {
    cardViewer match {
      case Some(window) =>
        window.webContents.send("change-card", value, color)
      case None =>
    }
  }

  private val watchingFrame: Frame = new Frame()
  watchingFrame.registerEvent(GameEvents.onActionTaken)((_: Frame, gameState: GameState, action: GameAction) => {
    gameState.lastTrick match {
      case Some(_) => trickViewer.show()
      case None => trickViewer.hide()
    }

    val state = gameState.state

    state match {
      case BettingState =>
        if (gameState.turnOfPlayer._1 == playerName) {
          showBetWindow(gameState)
        }
        ScoreBoard.setPlayerNames(gameState.points)
      case _ =>
    }

    state match {
      case PlayingCardState =>
        if (gameState.turnOfPlayer._1 == playerName) {
          feelingLucky.show()
        } else {
          feelingLucky.hide()
        }
      case _ =>
        feelingLucky.hide()
    }

    if (action.isInstanceOf[NewDeal]) {
      sendScoreHistory()
    }
  })

  def changeLastCardUnderMouse(x: Double, y: Double): Unit = {
    val underMouse = (List(
      TrumpFrame.underMouse(x, y),
      handFrame.cardUnderMouse(x, y),
      playTable.cardUnderMouse(x, y)
    ) ++ playerFrames.map(_.handFrame.cardUnderMouse(x, y))).find(_.isDefined) match {
      case Some(optionCard) => optionCard
      case None => None: Option[CardGraphics]
    }
    if (underMouse != lastCardUnderMouse) {
      if (underMouse.isDefined) {
        lastCardUnderMouse = underMouse
        sendCardToViewer(underMouse.get.card.value.value, underMouse.get.card.color.color)
      }
    }
  }
  TrumpFrame.setScript(ScriptKind.OnMouseMoved)((_: Frame, x: Double, y: Double, _: Double, _: Double, _: Int) => {
    changeLastCardUnderMouse(x, y)
  })
  UIParent.setScript(ScriptKind.OnMouseMoved)((_: Frame, x: Double, y: Double, _: Double, _: Double, _: Int) => {
    changeLastCardUnderMouse(x, y)
  })


  /**
   * Creates the necessary innerHtml needed for the score history.
   */
  def scoreHistoryInnerHTML: String = {
    case class PointInfo(player: String, bets: Int, tricks: Int, pointsWon: Int, totalPoints: Int)

    val allStates = gameActions.scanLeft(gameState)({case (gs, a) => a(gs)}).tail
    val pointsInfo: List[(Int, List[PointInfo])] = allStates
      .zip(allStates.tail)
      .zip(gameActions.tail)
      .filter(_._2.isInstanceOf[NewDeal])
      .unzip._1
      .map({case (state1, state2) =>
        // applying a new hand to compute the tricks
        val state = NewHand()(state1)
        (state1.nbrCardsDistributed, gameState.players.toList.map(player => PointInfo(
          player,
          state.bets(player), state.tricks(player),
          state2.points(player) - state.points(player), state2.points(player)
        )))
    })

    if (scala.scalajs.LinkingInfo.developmentMode)
      println(pointsInfo.map(_._2.mkString(", ")).mkString("\n"))

    val playerInfoWidth: Int = 95 / gameState.players.length
    val betTricksWonWidth: Int = playerInfoWidth * 20 / 100
    val pointsWidth: Int = playerInfoWidth - 3 * betTricksWonWidth
    val nbrCardsDistributedWidth = 100 - (pointsWidth + 3 * betTricksWonWidth) * gameState.players.length

    "<colgroup>\n<col style=\"border: 1px solid black; width: " + nbrCardsDistributedWidth + "%\">\n" +
      ((("<col style=\"border: 1px solid black; width: " + betTricksWonWidth + "%\">\n") * 3 +
        "<col style=\"border: 1px solid black; width: " + pointsWidth + "%\">\n") * gameState.players.length) +
      "</colgroup>\n" +
    "<thead>\n" +
    "<tr class=\"success\">\n<th>#</th>\n" +
      gameState.players.map("<th colspan=\"4\">" + _ + "</th>").mkString("\n") +
    "\n</tr>\n" +
      "<tr>\n<th></th>\n" +
      ("<th align=\"right\">B</th><th align=\"right\">T</th><th>W</th><th align=\"right\">Points</th>\n" *
        gameState.players.length) +
    "</tr>\n" +
      "</thead>\n<tbody>\n<tr>\n<td></td>" +
    gameState.players
      .map(("<td></td>" * 3) +
        "<td style=\"font-size: 140%\" align=\"right\" bgcolor=\"#E52617\"><font color=\"#FFFFFF\">" +
        gameState.points(_) + "</font></td>").mkString("\n") +
    "</tr>\n" +
    pointsInfo.map({case (nbrCards, list) =>
      "<tr>\n" +
      "<td>" + nbrCards + "</td>" +
      list.map(info => {
        "<td style=\"valign: middle\" align=\"right\">" + info.bets +
          "</td><td style=\"valign: middle\" align=\"right\">" + info.tricks +
          "</td><td style=\"valign: middle\" align=\"right\">" + info.pointsWon + "</td>" +
          "<td style=\"font-size: 140%; valign: middle\" bgcolor=\"#E52617\" align=\"right\">" +
          "<font color=\"#FFFFFF\">" + info.totalPoints + "</font></td>"
      }).mkString("\n") +
      "\n</tr>"
    }).mkString("\n") +
    "\n</tbody>"
  }

  private var scoreHistoryWindow: Option[BrowserWindow] = None
  private val scoreHistoryButton: ScoreHistoryButton = new ScoreHistoryButton(this, showCardViewerButton)
  private val scoreHistoryCloseHandler: js.Function1[Event, Unit] = (_: Event) => {
    try {
      scoreHistoryWindow = None
      scoreHistoryButton.show()
    } catch {
      case _: Throwable =>
    }
  }

  private def removeCloseHistoryHandler(): Unit = scoreHistoryWindow match {
    case Some(window) => window.removeAllListeners("close")
    case None =>
  }


  def sendScoreHistory(): Unit = scoreHistoryWindow match {
    case Some(window) => window.webContents.send("send-info", scoreHistoryInnerHTML)
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

    scoreHistoryWindow.get.webContents.on("did-finish-load", () => sendScoreHistory())
  }



  def sendPlayCard(p: String, card: Card): Unit = {
    if (p == playerName) {
      val action = PlayCard(playerName, card)
      if (currentGameState.legalActions(action)) {
        sendReliable(action.toMessage(gameName))
      } else {
        // TODO: show error message to the player with a dedicated message.
        if (scala.scalajs.LinkingInfo.developmentMode)
          println(currentGameState.toString)

        println("I can't play that card now.")
        val gameState = currentGameState
        gameState.state match {
        case PlayingCardState =>
          if (playerName != gameState.turnOfPlayer._1) {
            println(s"It's the turn of player ${gameState.turnOfPlayer._1}")
          } else if (!gameState.hands(playerName).contains(card)) {
              println(s"Your hand does not contain $card")
          } else {
            println(s"starting color empty: ${gameState.startingColor.isEmpty}")
            if (gameState.startingColor.isDefined) {
              println(s"color of your card: ${card.color} against starting color ${gameState.startingColor.get}")
              println(s"your hand contains the starting color: ${gameState.hands(playerName).exists(card => {
                card.color == gameState.startingColor.get
              })}")
              println(s"your hand: ${gameState.hands(playerName)}")
            }
          }

          case s =>
            println(s)
        }
      }
    }
  }

  private def fillEmptyHands(gameState: GameState = currentGameState): List[GameAction] = {
    val actions = gameState.players.filterNot(gameState.hands.isDefinedAt).map(PlayerReceivesHand(_, Array()))
    gameActions ++= actions
    ScriptObject.firesEvent(GameEvents.onPlayersStartBets)
    actions.toList
  }

  private def cardMessage2Card(cardMessage: CardMessage): Card =
    DefaultCard(DefaultCardValue(cardMessage.value), CardColor(cardMessage.color))

  def messageCallback(client: Client, msg: Message): Unit = {
    msg match {
      case PlayCardMessage(_, player, cardMessage) =>
        val action = PlayCard(player, cardMessage2Card(cardMessage))
        gameActions :+= action
        ScriptObject.firesEvent(GameEvents.onPlayerPlaysCard)(player, cardMessage2Card(cardMessage))
        ScriptObject.firesEvent(GameEvents.onActionTaken)(currentGameState, action)

      case BetTrickNumberMessage(_, player, bet) =>
        val action = BetTrickNumber(player, bet)
        gameActions :+= action
        ScriptObject.firesEvent(GameEvents.onPlayerBets)(player, bet)
        ScriptObject.firesEvent(GameEvents.onActionTaken)(currentGameState, action)

      case PlayerReceivesHandMessage(_, player, cardMessages) =>
        val cards = cardMessages.map(cardMessage2Card)
        val action = PlayerReceivesHand(player, cards)
        gameActions :+= action
        val state = currentGameState
        ScriptObject.firesEvent(GameEvents.onPlayerReceivesHand)(player, cards.toList)
        ScriptObject.firesEvent(GameEvents.onActionTaken)(state, action)

      case ChooseTrumpMessage(_, trumpCard) =>
        val current = currentGameState
        val fillingHandActions = fillEmptyHands(current)
        val action = ChooseTrump(cardMessage2Card(trumpCard))
        gameActions :+= action
        ScriptObject.firesEvent(GameEvents.onChooseTrump)(cardMessage2Card(trumpCard))
        ScriptObject.firesEvent(GameEvents.onActionTaken)(current(fillingHandActions :+ action), action)

      case NewHandMessage(_) =>
        val current = currentGameState
        val action = NewHand()
        gameActions :+= action
        ScriptObject.firesEvent(GameEvents.onNewHand)
        ScriptObject.firesEvent(GameEvents.onActionTaken)(action(current), action)

      case NewDealMessage(_, successBonus, failurePenalty, bonusPerTrick, penaltyPerTrick) =>
        val action = NewDeal(successBonus, failurePenalty, bonusPerTrick, penaltyPerTrick)
        gameActions :+= action
        val current = currentGameState
        ScriptObject.firesEvent(GameEvents.onNewDeal)(successBonus, failurePenalty, bonusPerTrick, penaltyPerTrick)
        ScriptObject.firesEvent(GameEvents.onActionTaken)(current, action)
        if (current.state == GameEnded) {
          showCardViewerButton.hide()
          scoreHistoryButton.hide()
        }

      case GameStarts(_, _) =>
        gameStarts()

      case StillWaitingForPlayers(_, n) =>
        println(s"still waiting for $n players")

      case ClosingGame(_, message) =>
        cardViewer match {
          case Some(window) =>
            window.close()
          case _ =>
        }

        removeCloseHistoryHandler()
        dom.window.onbeforeunload = null

        if (message == "gameEndedNormally") {
          VariableStorage.storeValue(
            "endGamePoints",
            currentGameState
              .points
              .toArray
              .flatMap(elem => List(elem._1, elem._2.toString))
              .toJSArray
          )
          disconnect()
          dom.window.location.href = "./scoreboard.html"
        } else {
          dom.window.alert(message)
          dom.window.location.href = "../../gamemenus/mainscreen/mainscreen.html"
        }

      case m: InGameChatMessage =>
        chatHandler.receivedChatMessage(m)

      case _ =>
        println(s"Unknown message: $msg")
    }
  }

  override def connectedCallback(client: Client, peer: Peer, connected: Boolean): Unit = {
    if (connected) {
      sendReliable(PlayerConnecting(gameName, playerName, password))
    } else if (currentGameState.state != GameEnded) {
      dom.window.alert("You have been disconnected from the server.")
      dom.window.location.href = "../../gamemenus/mainscreen/mainscreen.html"
    }
  }

  Engine.painter.setBackgroundColor(24 / 255.0, 77 / 255.0, 30 / 255.0)
  val state = new GameRunner(draw = () => {

    playTable.draw()
    /** HandFrame is a Frame that is drawn automatically by drawAllFrames methods. */
    Frame.drawAllFrames()
  },
    keyPressed = (key: String, isRepeat: Boolean) => {
      Frame.keyPressed(key, isRepeat)
    },
    keyReleased = (key: String) => {
      Frame.keyReleased(key)
    },
    mousePressed = (x: Double, y: Double, button: Int) => {
      Frame.clickHandler(x, y, button)
    },
    mouseMoved = (x: Double, y: Double, dx: Double, dy: Double, button: Int) => {
      Frame.mouseMoved(x, y, dx, dy, button)
    },
    mouseReleased = (x: Double, y: Double, button: Int) => {
      Frame.mouseReleased(x, y, button)
    },
    mouseWheel = (dx: Int, dy: Int, _: Int) => {
      Frame.wheelMoved(dx, dy)
    },
    update = (dt: Double) => {
      Frame.updateHandler(dt)
    })

  Engine.changeGameState(state)
}
