package io

import boopickle.Default._
import boopickle.CompositePickler

sealed abstract class FileContent


object FileContent {
  implicit val fileContentPickler: CompositePickler[FileContent] = {
    compositePickler[FileContent]
      .addConcreteType[ConnectionToGameInfo]
  }

}


final case class ConnectionToGameInfo(pseudo: String, gameName: String, address: String, port: Int) extends FileContent
