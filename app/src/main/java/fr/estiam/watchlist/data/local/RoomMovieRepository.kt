package fr.estiam.watchlist.data.local

import android.util.Log
import androidx.paging.PagingSource
import androidx.room.withTransaction
import fr.estiam.watchlist.data.MovieRepository
import fr.estiam.watchlist.model.CatalogueQuery
import fr.estiam.watchlist.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Implémentation Room du contrat [MovieRepository] (CRUD n° 1 + import JSON).
 *
 * @param loadSeedJson fonction qui renvoie le contenu de assets/movies.json.
 *        Passée en paramètre pour ne pas dépendre directement d'un Context
 *        (plus simple à tester et à expliquer).
 */
class RoomMovieRepository(
    private val database: WatchListDatabase,
    private val loadSeedJson: () -> String
) : MovieRepository {

    private val dao = database.movieDao()

    /** On traduit l'objet CatalogueQuery en paramètres de la requête SQL. */
    override fun pagingSource(query: CatalogueQuery): PagingSource<Int, Movie> =
        dao.pagingSource(
            search = query.search.trim(),
            genre = query.genre?.name,
            favoritesOnly = query.favoritesOnly,
            hideWatched = query.hideWatched,
            sort = query.sort.name
        )

    override fun observeMovie(id: String): Flow<Movie?> = dao.observeById(id)

    override suspend fun getMovie(id: String): Movie? = dao.getById(id)

    override suspend fun saveMovie(movie: Movie) {
        // Dernier rempart : même si un écran oublie de valider, rien d'invalide n'entre en base.
        require(movie.isValid()) { "Film invalide : ${movie.validate()}" }
        dao.upsert(movie.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteMovie(id: String) = dao.deleteById(id)

    override suspend fun setFavorite(id: String, favorite: Boolean) =
        dao.setFavorite(id, favorite, System.currentTimeMillis())

    override suspend fun setRating(id: String, rating: Int?) {
        require(rating == null || rating in 1..5) { "Note invalide : $rating" }
        dao.setRating(id, rating, System.currentTimeMillis())
    }

    /**
     * Import du JSON à la PREMIÈRE installation uniquement.
     * Appelée à chaque ouverture du catalogue : doit être rapide et sans
     * effet quand l'import a déjà eu lieu.
     */
    override suspend fun seedIfNeeded() {
        // 1. Déjà fait ? On s'arrête tout de suite (cas de toutes les relances).
        if (dao.getMeta(KEY_SEEDED) != null) return

        // 2. Lecture + parsing + validation sur un thread d'arrière-plan.
        val result = withContext(Dispatchers.IO) { MovieJsonParser.parse(loadSeedJson()) }
        // Visible dans Logcat (filtre : tag "WatchListSeed") — utile pour la démo de débogage.
        Log.i(TAG, "Import JSON : ${result.movies.size} films valides, ${result.rejectedCount} rejetés")

        // 3. Transaction : les films ET le marqueur sont écrits ensemble, ou pas du tout.
        database.withTransaction {
            // Nouvelle vérification à l'intérieur de la transaction : si deux appels
            // démarrent en même temps, un seul fera l'import.
            if (dao.getMeta(KEY_SEEDED) == null) {
                // IGNORE : un film déjà présent (ex. modifié par l'utilisateur) n'est pas écrasé.
                dao.insertAllIgnoringExisting(result.movies)
                // Le marqueur empêche tout réimport, même si l'utilisateur
                // supprime ensuite des films importés.
                dao.putMeta(AppMeta(KEY_SEEDED, System.currentTimeMillis().toString()))
            }
        }
    }

    private companion object {
        const val TAG = "WatchListSeed"
        const val KEY_SEEDED = "seed_movies_done"
    }
}
