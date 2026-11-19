package fr.estiam.watchlist.data.local

import fr.estiam.watchlist.model.Genre
import fr.estiam.watchlist.model.Movie
import org.json.JSONArray
import org.json.JSONObject

/**
 * Lecture du fichier assets/movies.json : texte JSON → liste de films VALIDES.
 *
 * Volontairement indépendant d'Android (pas de Context) pour pouvoir
 * le tester avec JUnit sur l'ordinateur.
 */
object MovieJsonParser {

    /** Résultat : les films acceptés + le nombre d'entrées rejetées (affiché dans Logcat). */
    data class Result(val movies: List<Movie>, val rejectedCount: Int)

    fun parse(json: String, currentYear: Int = java.time.Year.now().value): Result {
        // 1. Le fichier est un tableau : [ { ... }, { ... } ]
        val array = JSONArray(json)
        val accepted = mutableListOf<Movie>()
        val seenIds = mutableSetOf<String>()   // pour ignorer les doublons DANS le fichier
        var rejected = 0

        for (i in 0 until array.length()) {
            // 2. Une entrée qui n'est pas un objet JSON est rejetée.
            val item = array.optJSONObject(i)
            val movie = item?.let { toMovieOrNull(it) }

            // 3. Validation avec les MÊMES règles que le formulaire (Movie.validate),
            //    puis contrôle des doublons : add() renvoie false si l'id est déjà vu.
            if (movie != null && movie.isValid(currentYear) && seenIds.add(movie.id)) {
                accepted += movie
            } else {
                rejected++
            }
        }
        return Result(accepted, rejected)
    }

    /**
     * Mapping champ par champ. Les fonctions opt... ne plantent pas si un
     * champ manque : elles renvoient une valeur par défaut, volontairement
     * invalide (-1, "") pour que validate() rejette l'entrée.
     */
    private fun toMovieOrNull(item: JSONObject): Movie? {
        // Genre inconnu → entrée rejetée (on ne devine pas un genre).
        val genre = Genre.entries.firstOrNull {
            it.name.equals(item.optString("genre").trim(), ignoreCase = true)
        } ?: return null

        return Movie(
            id = item.optString("id").trim(),
            title = item.optString("title").trim(),
            year = item.optInt("year", -1),
            genre = genre,
            durationMinutes = item.optInt("durationMinutes", -1),
            director = item.optString("director").trim(),
            synopsis = item.optString("synopsis").trim(),
            // rating facultatif : absent ou null dans le JSON → film non noté
            rating = if (item.isNull("rating")) null else item.optInt("rating"),
            isFavorite = item.optBoolean("isFavorite", false)
        )
    }
}
