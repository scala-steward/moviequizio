package io.moviequiz

import scala.util.Random

class GameController(conf: Config, movies: Movies, gameDayIndex: Int, ui: UI, storage: Storage):

  private val rand = Random(gameDayIndex)

  private val movieSlugsShuffled = rand
    .shuffle(movies.slugs)
    .take(conf.maxNbOfMoviesPerGame)
    .sortWith((a, b) => movies.slugsToPopularity(a) > movies.slugsToPopularity(b))

  private val movieTitles = movies.slugsToTitles.values.flatten.toSeq

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
      ui.renderGuessBox(movieTitles)

  private def startGame(): Unit =
    ui.renderTitleAndScore(score)
    displayMovie(score)
    ui.renderGuessBox(movieTitles)

  private def displayMovie(movieIndex: Int): Unit =
    val screenshotNumber = rand.nextInt(conf.nbOfScreenshotsPerMovie) + 1
    val url = s"${conf.cdn}/images/${movieSlugsShuffled(movieIndex)}/$screenshotNumber.avif"
    ui.renderScreenshot(url)

  private def guess(movieName: String): Unit =
    if movies.slugsToTitles(movieSlugsShuffled(score)).contains(movieName) then winRound()
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
  def apply(conf: Config, movies: Movies, gameDayIndex: Int, ui: UI, storage: Storage): GameController =
    new GameController(conf, movies, gameDayIndex, ui, storage)
