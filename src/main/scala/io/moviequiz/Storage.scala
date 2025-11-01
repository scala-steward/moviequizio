package io.moviequiz

import org.scalajs.dom.window.localStorage

case class Game(gameDayIndex: Int, score: Int, isFinished: Boolean)

class Storage:

  private enum Key:
    case GameDayId, Score, IsFinished

  def getGame(gameDayIndex: Int): Option[Game] =
    val gameDayIndex = getIntItem(Key.GameDayId)
    val score = getIntItem(Key.Score)
    val isFinished = getBoolItem(Key.IsFinished)
    if gameDayIndex.isEmpty || score.isEmpty || isFinished.isEmpty then None
    else Some(Game(gameDayIndex.get, score.get, isFinished.get))

  def saveGame(game: Game): Unit =
    setItem(Key.GameDayId, game.gameDayIndex.toString)
    setItem(Key.Score, game.score.toString)
    setItem(Key.IsFinished, game.isFinished.toString)

  def clear(): Unit =
    Key.values.foreach(k => localStorage.removeItem(k.toString))

  private def setItem(key: Key, value: String): Unit =
    localStorage.setItem(key.toString, value)

  private def getIntItem(key: Key): Option[Int] =
    Option(localStorage.getItem(key.toString)).map(_.toInt)

  private def getBoolItem(key: Key): Option[Boolean] =
    Option(localStorage.getItem(key.toString)).map(_.toBoolean)
