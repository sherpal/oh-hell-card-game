name := "Rikiki"


val commonSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.1",
  scalacOptions ++= Seq("-deprecation", "-feature", "-encoding", "utf-8")
)

lazy val `shared` = project.in(file("shared")).
  enablePlugins(ScalaJSPlugin).
  settings(commonSettings).
  settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1",
      "me.chrons" %%% "boopickle" % "1.2.5",
      "org.scalatest" %%% "scalatest" % "3.0.1" % "test",
      "be.adoeraene" %%% "scala-canvas-gui" % "0.1.0-SNAPSHOT"
    ),
    scalaJSModuleKind := ModuleKind.CommonJSModule
  )

lazy val `gamePlaying` = project.in(file("gameplaying")).
  enablePlugins(ScalaJSPlugin).
  settings(commonSettings).
  settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1",
      "me.chrons" %%% "boopickle" % "1.2.5",
      "be.adoeraene" %%% "scala-canvas-gui" % "0.1.0-SNAPSHOT"
    ),
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    scalaJSUseMainModuleInitializer := true
  ).
  dependsOn(`shared`)


lazy val `gameMenus` = project.in(file("gameMenus")).
  enablePlugins(ScalaJSPlugin).
  settings(commonSettings).
  settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1",
      "me.chrons" %%% "boopickle" % "1.2.5",
      "be.adoeraene" %%% "scala-canvas-gui" % "0.1.0-SNAPSHOT"
    ),
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(`shared`)
  .dependsOn(`server`)


lazy val `main` = project.in(file("main")).
  enablePlugins(ScalaJSPlugin).
  settings(commonSettings).
  settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1",
      "me.chrons" %%% "boopickle" % "1.2.5",
      "be.adoeraene" %%% "scala-canvas-gui" % "0.1.0-SNAPSHOT"
    ),
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    scalaJSUseMainModuleInitializer := true,
    scalaJSOutputWrapper := ("global.myGlobalDirname = __dirname;", "")
  ).
  dependsOn(`shared`)


lazy val `server` = project.in(file("server")).
  enablePlugins(ScalaJSPlugin).
  settings(commonSettings).
  settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1",
      "me.chrons" %%% "boopickle" % "1.2.5",
      "be.adoeraene" %%% "scala-canvas-gui" % "0.1.0-SNAPSHOT"
    ),
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    scalaJSUseMainModuleInitializer := true
  ).
  dependsOn(`shared`)


