package io.moviequiz

import scala.scalajs.js.Date
import scala.util.Random

case class Movie(slug: String, name: String)

class GameController(now: Date, conf: Config, ui: UI, storage: Storage):

  private val moviesOrdered = List(
    Movie("bound-(1996)", "Bound (1996)"),
    Movie("le-roi-lion-(1994)", "Le Roi Lion (1994)"),
    Movie("never-back-down-(2008)", "Never Back Down (2008)"),
    Movie("the-guest-(2014)", "The Guest (2014)")
  )

  private val gameDayIndex: Int =
    val rootDate = new Date("2025-10-22")
    ((now.getTime() - rootDate.getTime()) / (1000 * 60 * 60 * 24)).toInt

  private val rand = Random(gameDayIndex)

  private val movies = rand.shuffle(moviesOrdered)

  private var score = 0

  def init(): Unit =
    ui.onStart = () => startGame()
    ui.onGuess = movieName => guess(movieName)
    storage.getGame(gameDayIndex) match
      case Some(game) if game.gameDayIndex == gameDayIndex =>
        loadGame(game)
      case Some(game) =>
        storage.clear()
        ui.renderWelcomeScreen()
      case None =>
        ui.renderWelcomeScreen()

  private def loadGame(game: Game): Unit =
    for i <- 0 until game.score - 1 do rand.nextInt()
    score = game.score
    if game.isFinished && isVictory then
      displayMovie(score - 1)
      ui.renderVictoryScreen(score, gameDayIndex)
    else if game.isFinished then
      if score > 0 then rand.nextInt()
      displayMovie(score)
      ui.renderFailScreen(score, gameDayIndex)
    else
      rand.nextInt()
      displayMovie(score)
      ui.renderTitleAndScore(score)
      ui.renderGuessBox(movies.map(_.name))

  private def startGame(): Unit =
    ui.renderTitleAndScore(score)
    displayMovie(score)
    ui.renderGuessBox(movies.map(_.name))

  private def displayMovie(movieIndex: Int): Unit =
    val movie = movies(movieIndex)
    val screenshotNumber = rand.nextInt(conf.nbOfScreenshotsPerMovie) + 1
    val url = s"${conf.cdn}/images/${movie.slug}/screenshot$screenshotNumber.jpg"
    ui.renderScreenshot(url)

  private def guess(movieName: String): Unit =
    if movieName == movies(score).name then winRound()
    else lose()

  private def isVictory = score == conf.maxNbOfMoviesPerGame

  private def winRound(): Unit =
    score += 1
    if isVictory then
      ui.renderVictoryScreen(score, gameDayIndex)
      storage.saveGame(Game(gameDayIndex, score, true))
    else
      ui.refreshScore(score)
      ui.clearGuessBox()
      displayMovie(score)
      storage.saveGame(Game(gameDayIndex, score, false))

  private def lose(): Unit =
    ui.renderFailScreen(score, gameDayIndex)
    storage.saveGame(Game(gameDayIndex, score, true))

object GameController:
  def apply(now: Date, conf: Config, ui: UI, storage: Storage): GameController =
    new GameController(now, conf, ui, storage)
