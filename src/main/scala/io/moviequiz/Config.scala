package io.moviequiz

case class Config(
    cdn: String = "https://cdn.moviequiz.io",
    maxNbOfMoviesPerGame: Int = 3,
    nbOfScreenshotsPerMovie: Int = 3
)
