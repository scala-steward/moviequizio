enablePlugins(ScalaJSPlugin)

import org.scalajs.linker.interface.ModuleSplitStyle
import sbt.io.IO
import java.nio.charset.StandardCharsets

name := "MovieQuiz.io"

scalaVersion := "3.8.4"

scalaJSUseMainModuleInitializer := true

libraryDependencies ++= Seq(
  "org.scala-js" %% "scalajs-dom" % "2.8.1",
  "org.scalatest" %% "scalatest-funspec" % "3.2.20" % Test,
  "org.scalamock" %% "scalamock" % "7.5.5" % Test
)

Compile / fullLinkJS / scalaJSLinkerConfig ~= {
  _.withSourceMap(false)
}

lazy val buildPublic = taskKey[Unit]("Build public directory")

buildPublic := {
  val base = (ThisBuild / baseDirectory).value
  val public = base / "public"

  val js = (Compile / fullLinkJS / scalaJSLinkedFile).value.data

  IO.copyFile(base / "index.html", public / "index.html")

  val indexPath = public / "index.html"
  val index = IO.read(indexPath)
  IO.write(
    indexPath,
    index.replaceAll(
      """<script type="text/javascript" src=".*"></script>""",
      """<script type="text/javascript" src="assets/main.js"></script>"""
    ),
    StandardCharsets.UTF_8,
    append = false
  )

  IO.copyFile(base / "favicon.ico", public / "favicon.ico")
  IO.copyDirectory(base / "assets", public / "assets")
  IO.copyFile(js, public / "assets" / "main.js")

  streams.value.log.info(s"Built public directory: ${public.getAbsolutePath}")
}
