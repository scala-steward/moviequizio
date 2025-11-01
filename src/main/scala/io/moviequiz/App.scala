package io.moviequiz

import org.scalajs.dom
import org.scalajs.dom.document

import scala.scalajs.js.Date

object App:

  def main(args: Array[String]): Unit =
    println("Welcome to MovieQuiz.io!")

    val now = new Date()
    val conf = Config()
    val ui = UI()
    val storage = Storage()
    val gameController = GameController(now, conf, ui, storage)

    document.addEventListener(
      "DOMContentLoaded",
      (e: dom.Event) => gameController.init()
    )
