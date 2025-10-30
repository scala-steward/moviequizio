package io.moviequiz

import scala.scalajs.js.Date
import scala.util.Random

case class Movie(slug: String, name: String)

class GameController(ui: UI):

  private val cdn = "https://cdn.moviequiz.io"

  private val maxNbOfMoviesPerGame = 3

  private val nbOfScreenshotsPerMovie = 3

  private val moviesOrdered = List(
    Movie("bound-(1996)", "Bound (1996)"),
    Movie("le-roi-lion-(1994)", "Le Roi Lion (1994)"),
    Movie("never-back-down-(2008)", "Never Back Down 2008)"),
    Movie("the-guest-(2014)", "The Guest (2014)")
  )

  private val gameDayId: Int =
    val rootDate = new Date(2025, 9, 22)
    val currentDate = new Date()
    ((currentDate.getTime() - rootDate.getTime()) / (1000 * 60 * 60 * 24)).toInt

  private val rand = Random(gameDayId)

  private val movies = rand.shuffle(moviesOrdered)

  private var score = 0

  def init(): Unit =
    ui.onStart = () => startGame()
    ui.onGuess = movieName => guess(movieName)
    Storage.loadGame(gameDayId) match
      case Some(game) if game.gameDayId == gameDayId =>
        loadGame(game)
      case Some(game) =>
        Storage.clear()
        ui.renderWelcomeScreen()
      case None =>
        ui.renderWelcomeScreen()

  private def loadGame(game: Game): Unit =
    movies.foreach(m => println(m.name))
    for i <- 0 until game.score - 1 do rand.nextInt()
    score = game.score
    if game.isFinished && isVictory then
      displayMovie(score - 1)
      ui.renderVictoryScreen(score, gameDayId)
    else if game.isFinished then
      if score > 0 then rand.nextInt()
      displayMovie(score)
      ui.renderFailScreen(score, gameDayId)
    else
      displayMovie(score)
      ui.renderTitleAndScore()
      ui.refreshScore(score)
      ui.renderGuessBox(movies.map(_.name))

  private def startGame(): Unit =
    ui.renderTitleAndScore()
    displayMovie(0)
    ui.renderGuessBox(movies.map(_.name))

  private def displayMovie(movieIndex: Int): Unit =
    val movie = movies(movieIndex)
    val screenshotNumber = rand.nextInt(nbOfScreenshotsPerMovie) + 1
    val url = s"$cdn/images/${movie.slug}/screenshot$screenshotNumber.jpg"
    ui.renderScreenshot(url)

  private def guess(movieName: String): Unit =
    if movieName == movies(score).name then winRound()
    else lose()

  private def isVictory = score == maxNbOfMoviesPerGame

  private def winRound(): Unit =
    score += 1
    if isVictory then
      ui.renderVictoryScreen(score, gameDayId)
      Storage.saveGame(Game(gameDayId, score, true))
    else
      ui.refreshScore(score)
      ui.clearGuessBox()
      displayMovie(score)
      Storage.saveGame(Game(gameDayId, score, false))

  private def lose(): Unit =
    ui.renderFailScreen(score, gameDayId)
    Storage.saveGame(Game(gameDayId, score, true))

object GameController:
  def apply(ui: UI): GameController = new GameController(ui)
