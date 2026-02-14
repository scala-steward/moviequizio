enablePlugins(ScalaJSPlugin)

name := "MovieQuiz.io"
scalaVersion := "3.8.1"
scalaJSUseMainModuleInitializer := true
libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.1"
libraryDependencies += "org.scalatest" %%% "scalatest-funspec" % "3.2.19" % "test"
libraryDependencies += "org.scalamock" %%% "scalamock" % "7.5.5" % "test"

Compile / fullLinkJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) }

import sbt.io.IO
import java.nio.charset.StandardCharsets

lazy val buildPublic = taskKey[File]("Build public directory")

buildPublic := {
  val base = (ThisBuild / baseDirectory).value
  val public = base / "public"

  (Compile / fullLinkJS).value
  val js = base / "target" / s"scala-${scalaVersion.value}" / "moviequiz-io-opt" / "main.js"

  IO.copyFile(base / "index.html", public / "index.html")
  val indexPath = public / "index.html"
  val index = IO.read(indexPath)
  val indexFixed = index.replaceAll(
    f"""<script type="text/javascript" src=".*"></script>""",
    """<script type="text/javascript" src="assets/main.js"></script>"""
  )
  IO.write(indexPath, indexFixed, StandardCharsets.UTF_8, append = false)
  IO.copyFile(base / "favicon.ico", public / "favicon.ico")
  IO.copyDirectory(base / "assets", public / "assets")
  IO.copyFile(js, public / "assets" / "main.js")

  streams.value.log.info(s"Built public directory: ${public.getAbsolutePath}")
  public
}
