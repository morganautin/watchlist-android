package fr.estiam.watchlist.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import fr.estiam.watchlist.model.Watchlist
import fr.estiam.watchlist.model.WatchlistEntry
import fr.estiam.watchlist.model.WatchlistItem
import fr.estiam.watchlist.model.WatchlistSummary
import kotlinx.coroutines.flow.Flow

/**
 * DAO des listes et de leur contenu (tables "watchlists" et "watchlist_items").
 */
@Dao
interface WatchlistDao {

    // ------------------------------------------------------------------
    // LECTURE
    // ------------------------------------------------------------------

    /**
     * Écran « Mes listes » : chaque liste + sa progression (fonctionnalité métier n° 2).
     * LEFT JOIN : on garde aussi les listes vides (0 film).
     * Les alias totalCount / watchedCount correspondent aux champs de WatchlistSummary.
     */
    @Query(
        """
        SELECT w.*,
               COUNT(i.movieId) AS totalCount,
               COALESCE(SUM(CASE WHEN i.status = 'WATCHED' THEN 1 ELSE 0 END), 0) AS watchedCount
        FROM watchlists w
        LEFT JOIN watchlist_items i ON i.watchlistId = w.id
        GROUP BY w.id
        ORDER BY w.createdAt DESC
        """
    )
    fun observeSummaries(): Flow<List<WatchlistSummary>>

    @Query("SELECT * FROM watchlists WHERE id = :id")
    fun observeById(id: Long): Flow<Watchlist?>

    /**
     * Écran « Détail d'une liste » : les films de la liste (JOIN) avec leur statut.
     * m.* remplit le Movie @Embedded, i.status / i.addedAt remplissent le reste.
     */
    @Query(
        """
        SELECT m.*, i.status AS status, i.addedAt AS addedAt
        FROM watchlist_items i
        INNER JOIN movies m ON m.id = i.movieId
        WHERE i.watchlistId = :watchlistId
        ORDER BY i.addedAt DESC
        """
    )
    fun observeEntries(watchlistId: Long): Flow<List<WatchlistEntry>>

    /** Écran détail d'un film : dans quelles listes est-il déjà ? */
    @Query("SELECT watchlistId FROM watchlist_items WHERE movieId = :movieId")
    fun observeWatchlistIdsForMovie(movieId: String): Flow<List<Long>>

    // ------------------------------------------------------------------
    // ÉCRITURE (CRUD n° 2)
    // ------------------------------------------------------------------

    /** Renvoie l'id généré automatiquement par Room. */
    @Insert
    suspend fun insert(watchlist: Watchlist): Long

    @Update
    suspend fun update(watchlist: Watchlist)

    /** Supprime la liste ; ses lignes watchlist_items partent avec (CASCADE). */
    @Query("DELETE FROM watchlists WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** IGNORE : ajouter deux fois le même film à la même liste ne fait rien. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItem(item: WatchlistItem)

    @Query("DELETE FROM watchlist_items WHERE watchlistId = :watchlistId AND movieId = :movieId")
    suspend fun deleteItem(watchlistId: Long, movieId: String)

    /** status = nom de l'enum WatchStatus ("TO_WATCH", "WATCHING", "WATCHED"). */
    @Query("UPDATE watchlist_items SET status = :status WHERE watchlistId = :watchlistId AND movieId = :movieId")
    suspend fun updateStatus(watchlistId: Long, movieId: String, status: String)
}
