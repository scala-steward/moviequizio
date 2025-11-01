import io.moviequiz.{Config, Game, GameController, Storage, UI}
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec

import scala.scalajs.js.Date

class GameControllerSpec extends AnyFunSpec with GivenWhenThen with MockFactory:

  private val now = new Date("2025-11-01")

  private val testGameDayIndex: Int =
    val rootDate = new Date("2025-10-22")
    ((now.getTime() - rootDate.getTime()) / (1000 * 60 * 60 * 24)).toInt

  private val testConf = Config(cdn = "https://test.cdn.moviequiz.io")

  describe("init") {
    it("should register the ui.onStart callback as expected") {
      Given("a gameController with mocked dependencies")
      val ui = mock[UI]
      val storage = mock[Storage]
      val gameController = GameController(now, testConf, ui, storage)

      And("no game is returned from storage")
      storage.getGame.expects(testGameDayIndex).returns(None)

      And("we mock the calls to  render the welcome screen, screenshot, title, score and guess box")
      val renderWelcomeScreenCallHandler = (() => ui.renderWelcomeScreen()).expects()
      val url = "https://test.cdn.moviequiz.io/images/le-roi-lion-(1994)/screenshot1.jpg"
      val renderScreenshotCallHandler = ui.renderScreenshot.expects(url)
      val renderTitleAndScoreCallHandler = ui.renderTitleAndScore.expects(0)
      val renderGuessBoxCallHandler = ui.renderGuessBox.expects(
        Seq("Le Roi Lion (1994)", "The Guest (2014)", "Bound (1996)", "Never Back Down (2008)")
      )

      When("we init the game and invoke ui.onStart()")
      gameController.init()
      ui.onStart()

      Then("the title, score, screenshot and guess box should have been rendered")
      renderTitleAndScoreCallHandler.once()
      renderScreenshotCallHandler.once()
      renderGuessBoxCallHandler.once()
    }

    it("should register the ui.onGuess callback as expected for a correct answer leading to victory") {
      Given("a gameController with mocked dependencies")
      val ui = mock[UI]
      val storage = mock[Storage]
      val gameController = GameController(now, testConf.copy(maxNbOfMoviesPerGame = 1), ui, storage)

      And("no game is returned from storage")
      storage.getGame.expects(testGameDayIndex).returns(None)

      And("we mock the calls to render the welcome screen, victory screen and save the game to storage")
      val renderWelcomeScreenCallHandler = (() => ui.renderWelcomeScreen()).expects()
      val renderVictoryScreenCallHandler = ui.renderVictoryScreen.expects(1, testGameDayIndex)
      val saveGameCallHandler = storage.saveGame.expects(Game(testGameDayIndex, 1, true))

      When("we init the game and invoke ui.onGuess() with a correct answer")
      gameController.init()
      ui.onGuess("Le Roi Lion (1994)")

      Then("the victory screen should have been rendered and the game saved")
      renderVictoryScreenCallHandler.once()
      saveGameCallHandler.once()
    }

    it(
      "should register the ui.onGuess callback as expected for a correct answer not leading to victory yet"
    ) {
      Given("a gameController with mocked dependencies")
      val ui = mock[UI]
      val storage = mock[Storage]
      val gameController = GameController(now, testConf.copy(maxNbOfMoviesPerGame = 2), ui, storage)

      And("no game is returned from storage")
      storage.getGame.expects(testGameDayIndex).returns(None)

      And("we mock the calls to  render the welcome screen, refresh the score and clear the guess box")
      val renderWelcomeScreenCallHandler = (() => ui.renderWelcomeScreen()).expects()
      val refreshScoreCallHandler = ui.refreshScore.expects(1)
      val clearGuessBoxCallHandler = (() => ui.clearGuessBox()).expects()

      And("we mock the calls to render the screenshot and save the game to storage")
      val url = "https://test.cdn.moviequiz.io/images/the-guest-(2014)/screenshot1.jpg"
      val renderScreenshotCallHandler = ui.renderScreenshot.expects(url)
      val saveGameCallHandler = storage.saveGame.expects(Game(testGameDayIndex, 1, false))

      When("we init the game and invoke ui.onGuess() with a correct answer")
      gameController.init()
      ui.onGuess("Le Roi Lion (1994)")

      Then("the score should have been refreshed, guess box cleared, screenshot rendered and game saved")
      refreshScoreCallHandler.once()
      clearGuessBoxCallHandler.once()
      renderScreenshotCallHandler.once()
      saveGameCallHandler.once()
    }

    it("should register the ui.onGuess callback as expected for a wrong answer") {
      Given("a gameController with mocked dependencies")
      val ui = mock[UI]
      val storage = mock[Storage]
      val gameController = GameController(now, testConf, ui, storage)

      And("no game is returned from storage")
      storage.getGame.expects(testGameDayIndex).returns(None)

      And("we mock the calls to render the welcome screen, fail screen and save the game to storage")
      val renderWelcomeScreenCallHandler = (() => ui.renderWelcomeScreen()).expects()
      val renderFailScreenCallHandler = ui.renderFailScreen.expects(0, testGameDayIndex)
      val saveGameCallHandler = storage.saveGame.expects(Game(testGameDayIndex, 0, true))

      When("we init the game and invoke ui.onGuess() with a wrong answer")
      gameController.init()
      ui.onGuess("Never Back Down (2008)")

      Then("the fail screen should have been rendered and the game saved")
      renderFailScreenCallHandler.once()
      saveGameCallHandler.once()
    }

    it("should load the current game properly if one is returned from storage for a victory") {
      Given("a gameController with mocked dependencies")
      val ui = mock[UI]
      val storage = mock[Storage]
      val gameController = GameController(now, testConf, ui, storage)

      And("we mock the call to return a victorious game from storage")
      val game = makeGame(score = testConf.maxNbOfMoviesPerGame, isFinished = true)
      storage.getGame.expects(testGameDayIndex).returns(Some(game))

      And("we mock the calls to render the screenshot and victory screen")
      val url = "https://test.cdn.moviequiz.io/images/bound-(1996)/screenshot2.jpg"
      val renderScreenshotCallHandler = ui.renderScreenshot.expects(url)
      val renderVictoryScreenCallHandler = ui.renderVictoryScreen.expects(game.score, testGameDayIndex)

      When("we init the game")
      gameController.init()

      Then("the screenshot and victory screen should have been rendered")
      renderScreenshotCallHandler.once()
      renderVictoryScreenCallHandler.once()
    }

    it("should load the game properly if returned from storage for a defeat with a 0 score") {
      Given("a gameController with mocked dependencies")
      val ui = mock[UI]
      val storage = mock[Storage]
      val gameController = GameController(now, testConf, ui, storage)

      And("we mock the call to return a lost game with a 0 score from storage")
      val game = makeGame(score = 0, isFinished = true)
      storage.getGame.expects(testGameDayIndex).returns(Some(game))

      And("we mock the calls to render the screenshot and fail screen")
      val url = "https://test.cdn.moviequiz.io/images/le-roi-lion-(1994)/screenshot1.jpg"
      val renderScreenshotCallHandler = ui.renderScreenshot.expects(url)
      val renderFailScreenCallHandler = ui.renderFailScreen.expects(game.score, testGameDayIndex)

      When("we init the game")
      gameController.init()

      Then("the screenshot and fail screen should have been rendered")
      renderScreenshotCallHandler.once()
      renderFailScreenCallHandler.once()
    }

    it("should load the game properly if returned from storage for a non-zero defeat") {
      Given("a gameController with mocked dependencies")
      val ui = mock[UI]
      val storage = mock[Storage]
      val gameController = GameController(now, testConf, ui, storage)

      And("we mock the call to return a lost game with a non-zero defeat from storage")
      val game = makeGame(score = 1, isFinished = true)
      storage.getGame.expects(testGameDayIndex).returns(Some(game))

      And("we mock the calls to render the screenshot and fail screen")
      val url = "https://test.cdn.moviequiz.io/images/the-guest-(2014)/screenshot2.jpg"
      val renderScreenshotCallHandler = ui.renderScreenshot.expects(url)
      val renderFailScreenCallHandler = ui.renderFailScreen.expects(game.score, testGameDayIndex)

      When("we init the game")
      gameController.init()

      Then("the screenshot and fail screen should have been rendered")
      renderScreenshotCallHandler.once()
      renderFailScreenCallHandler.once()
    }

    it("should load the game properly if returned from storage in progress") {
      Given("a gameController with mocked dependencies")
      val ui = mock[UI]
      val storage = mock[Storage]
      val gameController = GameController(now, testConf, ui, storage)

      And("we mock the call to return an in-progress game from storage")
      val game = makeGame(score = 1, isFinished = false)
      storage.getGame.expects(testGameDayIndex).returns(Some(game))

      And("we mock the calls to render the screenshot, title, score and guess box")
      val url = "https://test.cdn.moviequiz.io/images/the-guest-(2014)/screenshot2.jpg"
      val renderScreenshotCallHandler = ui.renderScreenshot.expects(url)
      val renderTitleAndScoreCallHandler = ui.renderTitleAndScore.expects(game.score)
      val renderGuessBoxCallHandler = ui.renderGuessBox.expects(
        Seq("Le Roi Lion (1994)", "The Guest (2014)", "Bound (1996)", "Never Back Down (2008)")
      )

      When("we init the game")
      gameController.init()

      Then("the screenshot, title, score and guess box should have been rendered")
      renderScreenshotCallHandler.once()
      renderTitleAndScoreCallHandler.once()
      renderGuessBoxCallHandler.once()
    }

    it("should clear the storage and render the welcome screen if there is an old saved game") {
      Given("a gameController with mocked dependencies")
      val ui = mock[UI]
      val storage = mock[Storage]
      val gameController = GameController(now, testConf, ui, storage)

      And("we mock the call to return a game with a different game day index from today's from storage")
      val game = makeGame(testGameDayIndex - 1)
      storage.getGame.expects(testGameDayIndex).returns(Some(game))

      And("we mock the calls to clear the storage and render the welcome screen")
      val clearStorageCallHandler = (() => storage.clear()).expects()
      val renderWelcomeScreenCallHandler = (() => ui.renderWelcomeScreen()).expects()

      When("we init the game")
      gameController.init()

      Then("the storage should have been cleared and the welcome screen rendered")
      clearStorageCallHandler.once()
      renderWelcomeScreenCallHandler.once()
    }

    it("should render the welcome screen if there is no saved game to load") {
      Given("a gameController with mocked dependencies")
      val ui = mock[UI]
      val storage = mock[Storage]
      val gameController = GameController(now, testConf, ui, storage)

      And("we mock the call to return no game from storage and render the welcome screen")
      storage.getGame.expects(testGameDayIndex).returns(None)
      val renderWelcomeScreenCallHandler = (() => ui.renderWelcomeScreen()).expects()

      When("we init the game")
      gameController.init()

      Then("the welcome screen should have been rendered")
      renderWelcomeScreenCallHandler.once()
    }
  }

  private def makeGame(gameDayIndex: Int = testGameDayIndex, score: Int = 2, isFinished: Boolean = false) =
    Game(gameDayIndex, score, isFinished)
