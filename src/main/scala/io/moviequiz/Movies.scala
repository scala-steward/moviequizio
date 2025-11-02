package io.moviequiz

import scala.scalajs.js
import scala.scalajs.js.JSON

case class Movies(slugs: Seq[String], slugsToTitles: Map[String, Set[String]])

object Movies:
  def fromJsonText(text: String): Movies =
    val array = JSON
      .parse(text)
      .selectDynamic("movies")
      .asInstanceOf[js.Array[js.Dynamic]]

    val slugs = array.map(json => json.slug.asInstanceOf[String]).toSeq

    val slugsToTitles = array
      .map(movie =>
        movie.slug.asInstanceOf[String] -> Set(
          movie.title_og.asInstanceOf[String],
          movie.title_en.asInstanceOf[String],
          movie.title_fr.asInstanceOf[String]
        )
      )
      .toMap

    Movies(slugs, slugsToTitles)
