package io.moviequiz

import org.scalajs.dom.html.Button
import org.scalajs.dom.window.navigator
import org.scalajs.dom.{Event, KeyCode, KeyboardEvent, MouseEvent, document, html, window}

import scala.scalajs.js.timers.setTimeout

class UI:

  var onStart: () => Unit = () => ()

  var onGuess: String => Unit = (_: String) => ()

  private def createButton(textContent: String, title: String): Button =
    val button = document.createElement("button").asInstanceOf[html.Button]
    button.classList.add("pushable")
    button.title = title
    val shadow = document.createElement("span")
    shadow.classList.add("shadow")
    val edge = document.createElement("span")
    edge.classList.add("edge")
    val front = document.createElement("span")
    front.classList.add("front")
    front.textContent = textContent
    button.appendChild(shadow)
    button.appendChild(edge)
    button.appendChild(front)
    button

  def renderWelcomeScreen(): Unit =
    val title = document.createElement("h1").asInstanceOf[html.Heading]
    title.textContent = "MovieQuiz.io"
    title.classList.add("title")
    document.body.appendChild(title)

    val startButton = createButton("▶", "Play")
    document.body.appendChild(startButton)

    startButton.addEventListener(
      "click",
      (e: MouseEvent) =>
        title.remove()
        startButton.remove()
        onStart()
    )

  def renderTitleAndScore(score: Int): Unit =
    val title = document.createElement("h1").asInstanceOf[html.Heading]
    title.textContent = "MovieQuiz.io"
    title.classList.add("header")
    document.body.appendChild(title)

    val scoreHeading = document.createElement("h1").asInstanceOf[html.Heading]
    scoreHeading.id = "score"
    scoreHeading.classList.add("score")
    scoreHeading.textContent = s"Score: $score"
    document.body.appendChild(scoreHeading)

  def renderScreenshot(url: String): Unit =
    document.body.style.backgroundImage = s"url('$url')"
    document.body.style.backgroundPosition = "center"
    document.body.style.backgroundSize = "cover"
    document.body.style.backgroundRepeat = "no-repeat"

  def renderGuessBox(movieNames: Seq[String]): Unit =
    val container = document.createElement("div")
    container.classList.add("suggestion-container")

    val input = document.createElement("input").asInstanceOf[html.Input]
    input.placeholder = "Guess the movie..."

    val clearButton = document.createElement("span").asInstanceOf[html.Span]
    clearButton.title = "Clear"
    clearButton.classList.add("clear-btn")
    clearButton.innerHTML = "&times;"

    val suggestions = document.createElement("ul").asInstanceOf[html.UList]
    container.appendChild(input)
    container.appendChild(clearButton)
    container.appendChild(suggestions)
    document.body.appendChild(container)

    var filtered = Seq.empty[String]
    var indexHighlighted: Option[Int] = None

    def renderList(): Unit =
      if filtered.isEmpty then input.classList.remove("has-suggestions")
      else input.classList.add("has-suggestions")

      if indexHighlighted.isDefined then
        val items = suggestions.getElementsByTagName("li")
        items(indexHighlighted.get).classList.remove("highlighted")
        indexHighlighted = None

      suggestions.innerHTML = ""
      filtered.foreach { movieName =>
        val li = document.createElement("li").asInstanceOf[html.LI]
        li.textContent = movieName
        li.addEventListener("click", (_: Event) => selectValue(movieName))
        li.style.cursor = "pointer"
        suggestions.appendChild(li)
      }

    def selectValue(value: String): Unit =
      input.value = value
      filtered = List.empty[String]
      renderList()
      onGuess(input.value)

    input.addEventListener(
      "input",
      (_: Event) =>
        if input.value.nonEmpty then clearButton.style.display = "block"
        else clearButton.style.display = "none"
        if input.value.length > 2 then
          filtered = movieNames.filter(_.toLowerCase.contains(input.value.toLowerCase)).take(7)
        else filtered = List.empty[String]
        renderList()
    )

    input.addEventListener(
      "keydown",
      (e: KeyboardEvent) =>
        if e.keyCode == KeyCode.Enter && filtered.nonEmpty then
          e.preventDefault()
          if indexHighlighted.isDefined then selectValue(filtered(indexHighlighted.get))
          else selectValue(filtered.head)
        if e.keyCode == KeyCode.Tab && filtered.nonEmpty then
          e.preventDefault()
          val items = suggestions.getElementsByTagName("li")
          if indexHighlighted.isDefined then
            items(indexHighlighted.get).classList.remove("highlighted")
            if e.shiftKey then
              indexHighlighted = Some((indexHighlighted.get - 1 + filtered.size) % filtered.size)
            else indexHighlighted = Some((indexHighlighted.get + 1) % filtered.size)
          else indexHighlighted = Some(0)
          items(indexHighlighted.get).classList.add("highlighted")
    )

    input.addEventListener(
      "blur",
      (_: Event) =>
        window.setTimeout(
          () =>
            filtered = List.empty[String]
            renderList()
          ,
          150
        )
    )

    clearButton.addEventListener(
      "click",
      (_: Event) => clearInput(input, clearButton)
    )

    input.focus()

  private def clearInput(input: html.Input, clearButton: html.Span): Unit =
    input.value = ""
    clearButton.style.display = "none"
    input.focus()

  def refreshScore(newScore: Int): Unit =
    val score = document.getElementById("score")
    score.textContent = s"Score: $newScore"

  def clearGuessBox(): Unit =
    val input = document.getElementsByTagName("input").head.asInstanceOf[html.Input]
    val clearButton = document.getElementsByTagName("span").head.asInstanceOf[html.Span]
    clearInput(input, clearButton)

  private def renderEndScreen(titleText: String, finalScore: Int, gameDayIndex: Int): Unit =
    document.body.innerHTML = ""

    val container = document.createElement("div")
    container.classList.add("end-container")

    val title = document.createElement("h1").asInstanceOf[html.Heading]
    title.textContent = titleText
    title.classList.add("title")
    container.append(title)

    val score = document.createElement("h1")
    score.textContent = s"Score: $finalScore"
    container.append(score)

    val shareButton = createButton("Share", "Share")
    container.append(shareButton)
    val shareText = s"MovieQuiz.io #$gameDayIndex score: $finalScore"
    shareButton.addEventListener(
      "click",
      (_: Event) =>
        navigator.clipboard.writeText(shareText)
        shareButton.querySelector(".front").textContent = "Copied!"
        setTimeout(5000) {
          shareButton.querySelector(".front").textContent = "Share"
        }
    )

    val text = document.createElement("h2").asInstanceOf[html.Heading]
    text.textContent = "Come back tomorrow for another challenge!"
    container.append(text)

    document.body.appendChild(container)

  def renderVictoryScreen(finalScore: Int, gameDayIndex: Int): Unit =
    renderEndScreen("YOU WON", finalScore, gameDayIndex)

  def renderFailScreen(finalScore: Int, gameDayIndex: Int): Unit =
    renderEndScreen("GAME OVER", finalScore, gameDayIndex)
