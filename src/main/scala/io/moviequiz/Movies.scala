package io.moviequiz

import scala.scalajs.js
import scala.scalajs.js.JSON

case class Movies(
    slugs: Seq[String],
    slugsToTitles: Map[String, Set[String]],
    slugsToPopularity: Map[String, Int]
)

object Movies:
  def fromJsonText(text: String): Movies =
    val array = JSON
      .parse(text)
      .selectDynamic("movies")
      .asInstanceOf[js.Array[js.Dynamic]]

    val slugs = array.map(json => json.slug.asInstanceOf[String]).toSeq

    val slugsToTitles = array
      .map(movie => movie.slug.asInstanceOf[String] -> movie.titles.asInstanceOf[js.Array[String]].toSet)
      .toMap

    val slugsToPopularity = array
      .map(movie => movie.slug.asInstanceOf[String] -> movie.popularity.asInstanceOf[Int])
      .toMap

    Movies(slugs, slugsToTitles, slugsToPopularity)
