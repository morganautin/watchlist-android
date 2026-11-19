package fr.estiam.watchlist.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests JUnit des règles de validation et de calcul des modèles.
 * Ils tournent sur l'ordinateur (sans émulateur) :
 * clic droit sur le fichier → Run 'ModelValidationTest'.
 *
 * Schéma de chaque test : 1. préparer  2. agir  3. vérifier (assert).
 */
class ModelValidationTest {

    // Année fixe : les tests donnent le même résultat quelle que soit la date du jour.
    private val year = 2026

    /** Un film valide de référence, qu'on modifie avec copy() dans chaque test. */
    private val validMovie = Movie(
        id = "movie_001", title = "Inception", year = 2010,
        genre = Genre.SCIENCE_FICTION, durationMinutes = 148, rating = 5
    )

    @Test
    fun validMovie_hasNoError() {
        assertTrue(validMovie.validate(year).isEmpty())
        assertTrue(validMovie.isValid(year))
    }

    @Test
    fun blankTitle_isRejected() {
        val movie = validMovie.copy(title = "   ") // que des espaces = vide
        assertEquals(setOf(MovieError.EMPTY_TITLE), movie.validate(year))
    }

    @Test
    fun yearOutOfRange_isRejected() {
        // Avant le premier film de l'histoire, et trop loin dans le futur.
        assertTrue(MovieError.INVALID_YEAR in validMovie.copy(year = 1800).validate(year))
        assertTrue(MovieError.INVALID_YEAR in validMovie.copy(year = year + 6).validate(year))
        // Limites acceptées
        assertFalse(MovieError.INVALID_YEAR in validMovie.copy(year = 1888).validate(year))
        assertFalse(MovieError.INVALID_YEAR in validMovie.copy(year = year + 5).validate(year))
    }

    @Test
    fun invalidDurationAndRating_areRejected() {
        val movie = validMovie.copy(durationMinutes = 0, rating = 6)
        assertEquals(
            setOf(MovieError.INVALID_DURATION, MovieError.INVALID_RATING),
            movie.validate(year)
        )
        // null = film pas encore noté : c'est autorisé
        assertTrue(validMovie.copy(rating = null).isValid(year))
    }

    @Test
    fun watchlistName_mustNotBeBlankOrTooLong() {
        assertTrue(Watchlist(name = "Films de Noël").isValid())
        assertFalse(Watchlist(name = "").isValid())
        assertFalse(Watchlist(name = "x".repeat(Watchlist.MAX_NAME_LENGTH + 1)).isValid())
    }

    @Test
    fun watchlistProgress_isComputedAndSafeWhenEmpty() {
        val list = Watchlist(id = 1, name = "À voir")
        assertEquals(40, WatchlistSummary(list, totalCount = 10, watchedCount = 4).progressPercent)
        // Liste vide : 0 % au lieu d'un plantage (division par zéro)
        assertEquals(0, WatchlistSummary(list, totalCount = 0, watchedCount = 0).progressPercent)
    }
}
