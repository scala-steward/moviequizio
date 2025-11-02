package io.moviequiz

import org.scalajs.dom
import org.scalajs.dom.document

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Date

object App:

  private val gameDayIndex: Int =
    val rootDate = new Date("2025-10-22")
    ((new Date().getTime() - rootDate.getTime()) / (1000 * 60 * 60 * 24)).toInt

  def main(args: Array[String]): Unit =
    println("Welcome to MovieQuiz.io!")

    document.addEventListener(
      "DOMContentLoaded",
      (e: dom.Event) =>
        val conf = Config()

        getMovies(s"${conf.cdn}/movies.json").map(movies =>
          val ui = UI()
          val storage = Storage()
          val gameController = GameController(conf, movies, gameDayIndex, ui, storage)
          gameController.init()
        )
    )

  private def getMovies(url: String): Future[Movies] =
    dom
      .fetch(url)
      .toFuture
      .flatMap(response =>
        if response.ok then response.text().toFuture.map(Movies.fromJsonText)
        else Future.failed(new Exception(s"HTTP error: ${response.status}"))
      )
