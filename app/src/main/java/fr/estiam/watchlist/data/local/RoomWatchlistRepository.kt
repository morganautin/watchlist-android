package fr.estiam.watchlist.data.local

import fr.estiam.watchlist.data.WatchlistRepository
import fr.estiam.watchlist.model.WatchStatus
import fr.estiam.watchlist.model.Watchlist
import fr.estiam.watchlist.model.WatchlistEntry
import fr.estiam.watchlist.model.WatchlistItem
import fr.estiam.watchlist.model.WatchlistSummary
import kotlinx.coroutines.flow.Flow

/**
 * Implémentation Room du contrat [WatchlistRepository] (CRUD n° 2).
 * Les lectures renvoient des Flow : « Mes listes » et « Détail d'une liste »
 * se mettent à jour seuls quand un film est ajouté ou qu'un statut change.
 */
class RoomWatchlistRepository(database: WatchListDatabase) : WatchlistRepository {

    private val dao = database.watchlistDao()

    override fun observeSummaries(): Flow<List<WatchlistSummary>> = dao.observeSummaries()

    override fun observeWatchlist(id: Long): Flow<Watchlist?> = dao.observeById(id)

    override fun observeEntries(watchlistId: Long): Flow<List<WatchlistEntry>> =
        dao.observeEntries(watchlistId)

    override fun observeWatchlistIdsForMovie(movieId: String): Flow<List<Long>> =
        dao.observeWatchlistIdsForMovie(movieId)

    override suspend fun createWatchlist(name: String, description: String): Long {
        val watchlist = Watchlist(name = name.trim(), description = description.trim())
        require(watchlist.isValid()) { "Nom de liste invalide" }
        return dao.insert(watchlist)   // id = 0 → Room génère le vrai id
    }

    override suspend fun updateWatchlist(watchlist: Watchlist) {
        require(watchlist.isValid()) { "Nom de liste invalide" }
        dao.update(watchlist.copy(name = watchlist.name.trim(), description = watchlist.description.trim()))
    }

    override suspend fun deleteWatchlist(id: Long) = dao.deleteById(id)

    override suspend fun addMovie(watchlistId: Long, movieId: String) =
        dao.insertItem(WatchlistItem(watchlistId = watchlistId, movieId = movieId))

    override suspend fun removeMovie(watchlistId: Long, movieId: String) =
        dao.deleteItem(watchlistId, movieId)

    override suspend fun setStatus(watchlistId: Long, movieId: String, status: WatchStatus) =
        dao.updateStatus(watchlistId, movieId, status.name)
}
