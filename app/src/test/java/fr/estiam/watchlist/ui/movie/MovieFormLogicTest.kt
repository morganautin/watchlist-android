package fr.estiam.watchlist.ui.movie

import fr.estiam.watchlist.model.Genre
import fr.estiam.watchlist.model.Movie
import fr.estiam.watchlist.model.MovieError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests JUnit de la logique du formulaire : transformation de la saisie
 * brute (du texte) en film, avant validation.
 */
class MovieFormLogicTest {

    private val year = 2026

    @Test
    fun badNumbers_becomeValidationErrors() {
        // L'utilisateur a laissé l'année vide et tapé du texte dans la durée.
        val input = MovieFormInput(
            title = "  Mon film  ", year = "", duration = "abc",
            director = "", synopsis = "", genre = Genre.COMEDY
        )

        val movie = MovieFormViewModel.buildMovie(input, original = null)

        assertEquals("Mon film", movie.title)   // espaces retirés
        assertEquals(
            setOf(MovieError.INVALID_YEAR, MovieError.INVALID_DURATION),
            movie.validate(year)
        )
    }

    @Test
    fun editing_keepsIdRatingAndFavorite() {
        val original = Movie(
            id = "movie_001", title = "Inception", year = 2010, genre = Genre.SCIENCE_FICTION,
            durationMinutes = 148, rating = 5, isFavorite = true
        )
        val input = MovieFormInput(
            title = "Inception (version longue)", year = "2010", duration = "160",
            director = "Christopher Nolan", synopsis = "", genre = Genre.THRILLER
        )

        val edited = MovieFormViewModel.buildMovie(input, original)

        // Même id = modification (pas de doublon), note et favori conservés.
        assertEquals("movie_001", edited.id)
        assertEquals(5, edited.rating)
        assertTrue(edited.isFavorite)
        // Les champs saisis sont bien appliqués.
        assertEquals(160, edited.durationMinutes)
        assertEquals(Genre.THRILLER, edited.genre)
        assertTrue(edited.isValid(year))
    }
}
