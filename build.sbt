enablePlugins(ScalaJSPlugin)

name := "MovieQuiz.io"
scalaVersion := "3.7.3"
scalaJSUseMainModuleInitializer := true
libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.1"
libraryDependencies += "org.scalatest" %%% "scalatest-funspec" % "3.2.19" % "test"
libraryDependencies += "org.scalamock" %%% "scalamock" % "7.5.0" % "test"
