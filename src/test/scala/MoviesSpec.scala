import io.moviequiz.Movies
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec

class MoviesSpec extends AnyFunSpec with GivenWhenThen with MockFactory:

  describe("fromJsonText") {
    it("should parse JSON text to movies as expected") {
      Given("movies as JSON text")
      val moviesJsonText =
        """
        {
          "movies": [
            {
              "slug": "aladdin-(1992)",
              "titles": [
                "Aladdin (1992)"
              ]
            },
            {
              "slug": "the-fifth-element-(1997)",
              "titles": [
                "The Fifth Element (1997)",
                "Le Cinquième Élément (1997)"
              ]
            },
            {
              "slug": "the-gods-must-be-crazy-(1980)",
              "titles": [
                "The Gods Must Be Crazy (1980)",
                "Les dieux sont tombés sur la tête (1980)"
              ]
            },
            {
              "slug": "the-good-the-bad-and-the-ugly-(1966)",
              "titles": [
                "The Good, the Bad and the Ugly (1966)",
                "Le Bon, la Brute et le Truand (1966)",
                "Il buono, il brutto, il cattivo (1966)"
              ]
            }
          ]
        }
      """

      When("we parse the text to movies")
      val movies = Movies.fromJsonText(moviesJsonText)

      Then("the slugs should be as expected")
      val expectedSlugs = Seq(
        "aladdin-(1992)",
        "the-fifth-element-(1997)",
        "the-gods-must-be-crazy-(1980)",
        "the-good-the-bad-and-the-ugly-(1966)"
      )
      assert(movies.slugs == expectedSlugs)

      And("the slugsToTitles map should be as expected")
      val expectedMap = Map(
        "aladdin-(1992)" -> Set("Aladdin (1992)"),
        "the-fifth-element-(1997)" -> Set(
          "The Fifth Element (1997)",
          "Le Cinquième Élément (1997)"
        ),
        "the-gods-must-be-crazy-(1980)" -> Set(
          "The Gods Must Be Crazy (1980)",
          "Les dieux sont tombés sur la tête (1980)"
        ),
        "the-good-the-bad-and-the-ugly-(1966)" -> Set(
          "The Good, the Bad and the Ugly (1966)",
          "Le Bon, la Brute et le Truand (1966)",
          "Il buono, il brutto, il cattivo (1966)"
        )
      )
      assert(movies.slugsToTitles == expectedMap)
    }
  }
