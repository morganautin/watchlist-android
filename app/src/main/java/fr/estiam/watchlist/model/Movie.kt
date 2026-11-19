package fr.estiam.watchlist.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ENTITÉ 1/3 — Un film du catalogue.
 *
 * C'est à la fois :
 *  - le modèle utilisé par les écrans (catalogue, détail, formulaire) ;
 *  - une table Room "movies" (une ligne = un film).
 *
 * ⚠️ CONTRAT COMMUN : les noms des champs ci-dessous sont utilisés par
 * toute l'équipe. Ne pas les renommer sans prévenir l'autre membre.
 */
@Entity(
    tableName = "movies",
    // Index = accélère la recherche par titre et le filtre par genre.
    indices = [Index("title"), Index("genre")]
)
data class Movie(
    // Identifiant texte stable : "movie_001" pour les films du JSON,
    // un UUID pour les films créés dans l'app. Il ne change JAMAIS,
    // c'est ce qui évite les doublons lors de l'import JSON.
    @PrimaryKey val id: String,
    val title: String,
    val year: Int,
    val genre: Genre,               // Room enregistre l'enum en texte ("SCIENCE_FICTION")
    val durationMinutes: Int,
    val director: String = "",
    val synopsis: String = "",
    val rating: Int? = null,        // note de 1 à 5 étoiles, null = pas encore noté
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Contrôle de saisie : renvoie la liste des erreurs (vide = film valide).
     * Utilisé à 3 endroits : le formulaire, l'import JSON et le Repository.
     * @param currentYear passé en paramètre pour pouvoir le tester facilement.
     */
    fun validate(currentYear: Int = java.time.Year.now().value): Set<MovieError> {
        val errors = mutableSetOf<MovieError>()
        if (id.isBlank()) errors += MovieError.MISSING_ID
        if (title.isBlank()) errors += MovieError.EMPTY_TITLE
        if (title.length > MAX_TITLE_LENGTH) errors += MovieError.TITLE_TOO_LONG
        // 1888 = premier film de l'histoire ; on tolère les sorties à venir (+5 ans)
        if (year !in FIRST_MOVIE_YEAR..(currentYear + 5)) errors += MovieError.INVALID_YEAR
        if (durationMinutes !in 1..MAX_DURATION_MINUTES) errors += MovieError.INVALID_DURATION
        if (rating != null && rating !in 1..5) errors += MovieError.INVALID_RATING
        return errors
    }

    /** Raccourci : true si aucune erreur. */
    fun isValid(currentYear: Int = java.time.Year.now().value): Boolean = validate(currentYear).isEmpty()

    companion object {
        const val FIRST_MOVIE_YEAR = 1888
        const val MAX_TITLE_LENGTH = 100
        const val MAX_DURATION_MINUTES = 600
    }
}

/** Genres disponibles (liste fermée = filtre et statistiques simples). */
enum class Genre {
    ACTION, ANIMATION, COMEDY, DOCUMENTARY, DRAMA,
    FANTASY, HORROR, ROMANCE, SCIENCE_FICTION, THRILLER
}

/** Erreurs possibles d'un film : chaque écran choisit son message (strings.xml). */
enum class MovieError {
    MISSING_ID, EMPTY_TITLE, TITLE_TOO_LONG, INVALID_YEAR, INVALID_DURATION, INVALID_RATING
}
