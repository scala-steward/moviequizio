import io.moviequiz.{Config, Game, GameController, Movies, Storage, UI}
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec

import scala.scalajs.js.Date

class GameControllerSpec extends AnyFunSpec with GivenWhenThen with MockFactory:

  private val movies = Movies(
    slugs = Seq(
      "a-fistful-of-dollar-(1964)",
      "for-a-few-dollars-more-(1965))",
      "the-good-the-bad-and-the-ugly-(1966)",
      "they-call-me-trinity-(1970)",
      "my-name-is-nobody-(1973)",
      "babe-(1995)",
      "bean-(1997)",
      "i-robot-(2004)",
      "lethal-weapon-(1987)",
      "the-gods-must-be-crazy-(1980)",
      "total-recall-(1990)",
      "jumanji-(1985)",
      "12-angry-men-(1957)"
    ),
    slugsToTitles = Map(
      "a-fistful-of-dollar-(1964)" -> Set("A Fistful of Dollars (1964)", "Per un pugno di dollari"),
      "for-a-few-dollars-more-(1965))" -> Set("For A Few Dollars More (1965)", "Per qualche dollaro in più"),
      "the-good-the-bad-and-the-ugly-(1966)" -> Set("The Good, the Bad and the Ugly (1996)"),
      "they-call-me-trinity-(1970)" -> Set("They Call Me Trinity (1970)", "Lo chiamavano Trinità..."),
      "my-name-is-nobody-(1973)" -> Set("My Name Is Nobody (1973)"),
      "babe-(1995)" -> Set("Babe (1995)"),
      "bean-(1997)" -> Set("Bean (1997)"),
      "i-robot-(2004)" -> Set("I, Robot (2004)"),
      "lethal-weapon-(1987)" -> Set("Lethal Weapon (1987)"),
      "the-gods-must-be-crazy-(1980)" -> Set("The Gods Must Be Crazy (1980)"),
      "jumanji-(1985)" -> Set("Jumanji (1985)"),
      "12-angry-men-(1957)" -> Set("12 Angry Men (1957)")
    )
  )

  private val movieTitles = movies.slugsToTitles.values.flatten.toSeq

  private val conf = Config(cdn = "https://test.cdn.moviequiz.io")

  private val ui = mock[UI]

  private val storage = mock[Storage]

  private val gameDayIndex: Int =
    val rootDate = new Date("2025-10-22")
    ((new Date("2025-11-01").getTime() - rootDate.getTime()) / (1000 * 60 * 60 * 24)).toInt

  describe("init") {
    it("should register the ui.onStart callback as expected") {
      Given("a gameController with mocked dependencies")
      val gameController = GameController(conf, movies, gameDayIndex, ui, storage)

      And("no game is returned from storage")
      storage.getGame.expects(gameDayIndex).returns(None)

      And("we mock the calls to  render the welcome screen, screenshot, title, score and guess box")
      val renderWelcomeScreenCallHandler = (() => ui.renderWelcomeScreen()).expects()
      val url = "https://test.cdn.moviequiz.io/images/the-gods-must-be-crazy-(1980)/1.avif"
      val renderScreenshotCallHandler = ui.renderScreenshot.expects(url)
      val renderTitleAndScoreCallHandler = ui.renderTitleAndScore.expects(0)
      val renderGuessBoxCallHandler = ui.renderGuessBox.expects(movieTitles)

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
      val gameController =
        GameController(conf.copy(maxNbOfMoviesPerGame = 1), movies, gameDayIndex, ui, storage)

      And("no game is returned from storage")
      storage.getGame.expects(gameDayIndex).returns(None)

      And("we mock the calls to render the welcome screen, victory screen and save the game to storage")
      val renderWelcomeScreenCallHandler = (() => ui.renderWelcomeScreen()).expects()
      val renderVictoryScreenCallHandler = ui.renderVictoryScreen.expects(1, gameDayIndex)
      val saveGameCallHandler = storage.saveGame.expects(Game(gameDayIndex, 1, true))

      When("we init the game and invoke ui.onGuess() with a correct answer")
      gameController.init()
      ui.onGuess("The Gods Must Be Crazy (1980)")

      Then("the victory screen should have been rendered and the game saved")
      renderVictoryScreenCallHandler.once()
      saveGameCallHandler.once()
    }

    it(
      "should register the ui.onGuess callback as expected for a correct answer not leading to victory yet"
    ) {
      Given("a gameController with mocked dependencies")
      val gameController = GameController(conf, movies, gameDayIndex, ui, storage)

      And("no game is returned from storage")
      storage.getGame.expects(gameDayIndex).returns(None)

      And("we mock the calls to  render the welcome screen, refresh the score and clear the guess box")
      val renderWelcomeScreenCallHandler = (() => ui.renderWelcomeScreen()).expects()
      val refreshScoreCallHandler = ui.refreshScore.expects(1)
      val clearGuessBoxCallHandler = (() => ui.clearGuessBox()).expects()

      And("we mock the calls to render the screenshot and save the game to storage")
      val url = "https://test.cdn.moviequiz.io/images/12-angry-men-(1957)/1.avif"
      val renderScreenshotCallHandler = ui.renderScreenshot.expects(url)
      val saveGameCallHandler = storage.saveGame.expects(Game(gameDayIndex, 1, false))

      When("we init the game and invoke ui.onGuess() with a correct answer")
      gameController.init()
      ui.onGuess("The Gods Must Be Crazy (1980)")

      Then("the score should have been refreshed, guess box cleared, screenshot rendered and game saved")
      refreshScoreCallHandler.once()
      clearGuessBoxCallHandler.once()
      renderScreenshotCallHandler.once()
      saveGameCallHandler.once()
    }

    it("should register the ui.onGuess callback as expected for a wrong answer") {
      Given("a gameController with mocked dependencies")
      val gameController = GameController(conf, movies, gameDayIndex, ui, storage)

      And("no game is returned from storage")
      storage.getGame.expects(gameDayIndex).returns(None)

      And("we mock the calls to render the welcome screen, fail screen and save the game to storage")
      val renderWelcomeScreenCallHandler = (() => ui.renderWelcomeScreen()).expects()
      val renderFailScreenCallHandler = ui.renderFailScreen.expects(0, gameDayIndex)
      val saveGameCallHandler = storage.saveGame.expects(Game(gameDayIndex, 0, true))

      When("we init the game and invoke ui.onGuess() with a wrong answer")
      gameController.init()
      ui.onGuess("Babe (1995)")

      Then("the fail screen should have been rendered and the game saved")
      renderFailScreenCallHandler.once()
      saveGameCallHandler.once()
    }

    it("should load the current game properly if one is returned from storage for a victory") {
      Given("a gameController with mocked dependencies")
      val gameController = GameController(conf, movies, gameDayIndex, ui, storage)

      And("we mock the call to return a victorious game from storage")
      val game = makeGame(score = conf.maxNbOfMoviesPerGame, isFinished = true)
      storage.getGame.expects(gameDayIndex).returns(Some(game))

      And("we mock the calls to render the screenshot and victory screen")
      val url = "https://test.cdn.moviequiz.io/images/jumanji-(1985)/2.avif"
      val renderScreenshotCallHandler = ui.renderScreenshot.expects(url)
      val renderVictoryScreenCallHandler = ui.renderVictoryScreen.expects(game.score, gameDayIndex)

      When("we init the game")
      gameController.init()

      Then("the screenshot and victory screen should have been rendered")
      renderScreenshotCallHandler.once()
      renderVictoryScreenCallHandler.once()
    }

    it("should load the game properly if returned from storage for a defeat with a 0 score") {
      Given("a gameController with mocked dependencies")
      val gameController = GameController(conf, movies, gameDayIndex, ui, storage)

      And("we mock the call to return a lost game with a 0 score from storage")
      val game = makeGame(score = 0, isFinished = true)
      storage.getGame.expects(gameDayIndex).returns(Some(game))

      And("we mock the calls to render the screenshot and fail screen")
      val url = "https://test.cdn.moviequiz.io/images/the-gods-must-be-crazy-(1980)/1.avif"
      val renderScreenshotCallHandler = ui.renderScreenshot.expects(url)
      val renderFailScreenCallHandler = ui.renderFailScreen.expects(game.score, gameDayIndex)

      When("we init the game")
      gameController.init()

      Then("the screenshot and fail screen should have been rendered")
      renderScreenshotCallHandler.once()
      renderFailScreenCallHandler.once()
    }

    it("should load the game properly if returned from storage for a non-zero defeat") {
      Given("a gameController with mocked dependencies")
      val gameController = GameController(conf, movies, gameDayIndex, ui, storage)

      And("we mock the call to return a lost game with a non-zero defeat from storage")
      val game = makeGame(score = 1, isFinished = true)
      storage.getGame.expects(gameDayIndex).returns(Some(game))

      And("we mock the calls to render the screenshot and fail screen")
      val url = "https://test.cdn.moviequiz.io/images/12-angry-men-(1957)/1.avif"
      val renderScreenshotCallHandler = ui.renderScreenshot.expects(url)
      val renderFailScreenCallHandler = ui.renderFailScreen.expects(game.score, gameDayIndex)

      When("we init the game")
      gameController.init()

      Then("the screenshot and fail screen should have been rendered")
      renderScreenshotCallHandler.once()
      renderFailScreenCallHandler.once()
    }

    it("should load the game properly if returned from storage in progress") {
      Given("a gameController with mocked dependencies")
      val gameController = GameController(conf, movies, gameDayIndex, ui, storage)

      And("we mock the call to return an in-progress game from storage")
      val game = makeGame(score = 1, isFinished = false)
      storage.getGame.expects(gameDayIndex).returns(Some(game))

      And("we mock the calls to render the screenshot, title, score and guess box")
      val url = "https://test.cdn.moviequiz.io/images/12-angry-men-(1957)/1.avif"
      val renderScreenshotCallHandler = ui.renderScreenshot.expects(url)
      val renderTitleAndScoreCallHandler = ui.renderTitleAndScore.expects(game.score)
      val renderGuessBoxCallHandler = ui.renderGuessBox.expects(movieTitles)

      When("we init the game")
      gameController.init()

      Then("the screenshot, title, score and guess box should have been rendered")
      renderScreenshotCallHandler.once()
      renderTitleAndScoreCallHandler.once()
      renderGuessBoxCallHandler.once()
    }

    it("should clear the storage and render the welcome screen if there is an old saved game") {
      Given("a gameController with mocked dependencies")
      val gameController = GameController(conf, movies, gameDayIndex, ui, storage)

      And("we mock the call to return a game with a different game day index from today's from storage")
      val game = makeGame(gameDayIndex - 1)
      storage.getGame.expects(gameDayIndex).returns(Some(game))

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
      val gameController = GameController(conf, movies, gameDayIndex, ui, storage)

      And("we mock the call to return no game from storage and render the welcome screen")
      storage.getGame.expects(gameDayIndex).returns(None)
      val renderWelcomeScreenCallHandler = (() => ui.renderWelcomeScreen()).expects()

      When("we init the game")
      gameController.init()

      Then("the welcome screen should have been rendered")
      renderWelcomeScreenCallHandler.once()
    }
  }

  private def makeGame(gameDayIndex: Int = gameDayIndex, score: Int = 2, isFinished: Boolean = false) =
    Game(gameDayIndex, score, isFinished)
