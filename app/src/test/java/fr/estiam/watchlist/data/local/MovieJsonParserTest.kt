package fr.estiam.watchlist.data.local

import fr.estiam.watchlist.model.Genre
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Tests JUnit de l'import JSON : validation AVANT insertion en base.
 * Clic droit sur le fichier → Run 'MovieJsonParserTest'.
 */
class MovieJsonParserTest {

    private val year = 2026

    @Test
    fun bundledFile_has30ValidMovies_and3RejectedEntries() {
        // Le vrai fichier livré avec l'app (le dossier de travail des tests est app/).
        val json = File("src/main/assets/movies.json").readText()

        val result = MovieJsonParser.parse(json, year)

        // Exigence du projet : au moins 20 films valides.
        assertEquals(30, result.movies.size)
        // Les 3 entrées piégées (titre vide, durée négative, id en double) sont rejetées.
        assertEquals(3, result.rejectedCount)
        // Aucun doublon d'id parmi les films acceptés.
        assertEquals(result.movies.size, result.movies.map { it.id }.toSet().size)
    }

    @Test
    fun invalidEntries_areRejected() {
        val json = """
            [
              {"id":"a","title":"OK","year":2000,"genre":"drama","durationMinutes":90},
              {"id":"b","title":"Genre inconnu","year":2000,"genre":"WESTERN","durationMinutes":90},
              {"id":"c","title":"Sans année","genre":"DRAMA","durationMinutes":90},
              {"id":"","title":"Sans id","year":2000,"genre":"DRAMA","durationMinutes":90},
              {"id":"d","title":"Note 9","year":2000,"genre":"DRAMA","durationMinutes":90,"rating":9},
              42
            ]
        """.trimIndent()

        val result = MovieJsonParser.parse(json, year)

        // Seul "a" est valide (le genre en minuscules est accepté).
        assertEquals(listOf("a"), result.movies.map { it.id })
        assertEquals(Genre.DRAMA, result.movies.first().genre)
        assertEquals(5, result.rejectedCount)
    }

    @Test
    fun optionalFields_haveSafeDefaults() {
        val json = """
            [
              {"id":"x","title":"Sans note","year":2010,"genre":"COMEDY","durationMinutes":100,"rating":null},
              {"id":"y","title":"Favori noté","year":2011,"genre":"COMEDY","durationMinutes":100,"rating":4,"isFavorite":true}
            ]
        """.trimIndent()

        val (movies, rejected) = MovieJsonParser.parse(json, year)

        assertEquals(0, rejected)
        assertNull(movies[0].rating)          // null = pas encore noté
        assertFalse(movies[0].isFavorite)     // absent = pas favori
        assertEquals(4, movies[1].rating)
        assertTrue(movies[1].isFavorite)
        assertEquals("", movies[0].director)  // champ texte absent = chaîne vide
    }
}
