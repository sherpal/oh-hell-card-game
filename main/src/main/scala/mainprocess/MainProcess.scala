package mainprocess

import sharednodejsapis._

import scala.scalajs.js.{JSApp, UndefOr}
import electron.{App, BrowserWindowMainProcess}
import io.IO
import org.scalajs.dom.raw.Event

import scala.collection.mutable
import scala.scalajs.js



object MainProcess extends JSApp {
  def main(): Unit = {

    // Need to create the Storage object in order to use it.
    Storage

    val windows: mutable.Set[BrowserWindowMainProcess] = mutable.Set()


    def createWindow(): Unit = {
      val win = new BrowserWindowMainProcess(new BrowserWindowOptions {
        override val width: UndefOr[Int] = 1400
        override val height: UndefOr[Int] = 1000
      })


//      win.loadURL(
//        "file://" + Path.join(Path.dirname(js.Dynamic.global.myGlobalDirname.asInstanceOf[String]), "index.html")
//      )
//        "file://" + Path.join(js.Dynamic.global.selectDynamic("__dirname").asInstanceOf[String], "index.html")
//      )

      win.loadURL(
        "file://" + Path.join(IO.baseDirectory, "/gamemenus/mainscreen/mainscreen.html")
      )

      if (scala.scalajs.LinkingInfo.developmentMode)
        win.webContents.openDevTools()

      win.webContents.on("did-finish-load", () => {
        Storage.storeVariable(win.webContents, "windowId", win.id)
      })

      win.setMenu(null)
    }

    App.on("ready", () => createWindow())

    App.on("window-all-closed", () => App.quit())

    App.on("browser-window-created", (_: Event, window: BrowserWindowMainProcess) => {
      windows += window
      window.on("closed", () => windows -= window)
    })

    IPCMain.on("testing", (_: IPCMainEvent, a: Any) => {println(a)})

    IPCMain.on("print-to-pdf", (event: IPCMainEvent) => {
      event.sender.printToPDF(new PrintToPDFOptions {}, (e: js.Error, data: Buffer) => {
        println(e)
        val pdfFileName = Path.join(
          Path.dirname(IO.baseDirectory), "/scoresheet.pdf"
        )
        println(pdfFileName)
        FileSystem.writeFile(pdfFileName, data, (error: js.Error) => {
          println(error)
        })
        ElectronShell.openExternal(pdfFileName)
      })

      event.sender.send("printed")
    })
  }
}
