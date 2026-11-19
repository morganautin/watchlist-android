package fr.estiam.watchlist.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import fr.estiam.watchlist.model.GenreStat
import fr.estiam.watchlist.model.Movie
import fr.estiam.watchlist.model.StatusStat
import kotlinx.coroutines.flow.Flow

/**
 * DAO des films : toutes les requêtes SQL sur la table "movies".
 *
 * Rappel :
 *  - fonction `suspend`          → Room l'exécute hors du thread principal ;
 *  - retour `Flow` / `PagingSource` → réactif : Room relance la requête
 *    automatiquement dès que les tables concernées changent.
 */
@Dao
interface MovieDao {

    // ------------------------------------------------------------------
    // CATALOGUE (Paging 3) : recherche + filtres + tri en UNE requête.
    // ------------------------------------------------------------------
    // Chaque ligne du WHERE est un filtre « désactivable » :
    //   (:search = '' OR ...)        → si la recherche est vide, on garde tout ;
    //   (:genre IS NULL OR ...)      → null = tous les genres ;
    //   (:favoritesOnly = 0 OR ...)  → false = tous les films ;
    //   (:hideWatched = 0 OR ...)    → masque les films « vus » dans au moins une liste.
    // SQL ne permet pas un ORDER BY variable : on utilise des CASE, un par tri.
    // Seul le CASE correspondant à :sort renvoie une valeur, les autres renvoient NULL.
    @Query(
        """
        SELECT * FROM movies
        WHERE (:search = '' OR title LIKE '%' || :search || '%' OR director LIKE '%' || :search || '%')
          AND (:genre IS NULL OR genre = :genre)
          AND (:favoritesOnly = 0 OR isFavorite = 1)
          AND (:hideWatched = 0 OR id NOT IN
                (SELECT movieId FROM watchlist_items WHERE status = 'WATCHED'))
        ORDER BY
          CASE WHEN :sort = 'YEAR' THEN year END DESC,
          CASE WHEN :sort = 'RATING' THEN rating END DESC,
          title COLLATE NOCASE ASC
        """
    )
    fun pagingSource(
        search: String,
        genre: String?,          // nom de l'enum Genre (ex. "DRAMA") ou null
        favoritesOnly: Boolean,
        hideWatched: Boolean,
        sort: String             // nom de l'enum MovieSort ("TITLE", "YEAR", "RATING")
    ): PagingSource<Int, Movie>

    // ------------------------------------------------------------------
    // LECTURE D'UN FILM
    // ------------------------------------------------------------------

    /** Écran détail : émet à nouveau dès que ce film change (null s'il est supprimé). */
    @Query("SELECT * FROM movies WHERE id = :id")
    fun observeById(id: String): Flow<Movie?>

    /** Lecture ponctuelle (pré-remplir le formulaire). */
    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getById(id: String): Movie?

    // ------------------------------------------------------------------
    // ÉCRITURE (CRUD n° 1)
    // ------------------------------------------------------------------

    /** Ajout si l'id est nouveau, modification sinon. */
    @Upsert
    suspend fun upsert(movie: Movie)

    /** Suppression : les lignes de watchlist_items liées partent aussi (CASCADE). */
    @Query("DELETE FROM movies WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE movies SET isFavorite = :favorite, updatedAt = :now WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean, now: Long)

    @Query("UPDATE movies SET rating = :rating, updatedAt = :now WHERE id = :id")
    suspend fun setRating(id: String, rating: Int?, now: Long)

    /**
     * Import initial : IGNORE = si un film avec le même id existe déjà
     * (même modifié par l'utilisateur), il n'est PAS écrasé.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnoringExisting(movies: List<Movie>)

    // ------------------------------------------------------------------
    // STATISTIQUES (fonctionnalité métier n° 1) — toutes réactives
    // ------------------------------------------------------------------

    @Query("SELECT COUNT(*) FROM movies")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM movies WHERE isFavorite = 1")
    fun observeFavoriteCount(): Flow<Int>

    /** AVG ignore les films non notés (rating NULL) ; null si aucun film noté. */
    @Query("SELECT AVG(rating) FROM movies")
    fun observeAverageRating(): Flow<Double?>

    /** Les alias AS doivent porter le même nom que les champs de GenreStat. */
    @Query(
        """
        SELECT genre, COUNT(*) AS count, AVG(rating) AS averageRating
        FROM movies GROUP BY genre ORDER BY count DESC
        """
    )
    fun observeGenreStats(): Flow<List<GenreStat>>

    /** Nombre de films par statut, toutes listes confondues. */
    @Query("SELECT status, COUNT(*) AS count FROM watchlist_items GROUP BY status")
    fun observeStatusStats(): Flow<List<StatusStat>>

    // ------------------------------------------------------------------
    // TABLE TECHNIQUE (import JSON une seule fois)
    // ------------------------------------------------------------------

    @Query("SELECT value FROM app_meta WHERE `key` = :key")
    suspend fun getMeta(key: String): String?

    @Upsert
    suspend fun putMeta(meta: AppMeta)
}
