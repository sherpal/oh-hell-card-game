package mainprocess

import sharednodejsapis.{BrowserWindowOptions, Path}

import scala.scalajs.js.{JSApp, UndefOr}
import electron.{App, BrowserWindowMainProcess}
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
        "file://" + Path.join(
          Path.dirname(js.Dynamic.global.myGlobalDirname.asInstanceOf[String]), "/gamemenus/mainscreen/mainscreen.html"
        )
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
  }
}
