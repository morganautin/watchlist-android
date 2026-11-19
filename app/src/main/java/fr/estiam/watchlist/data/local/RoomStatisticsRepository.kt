package fr.estiam.watchlist.data.local

import fr.estiam.watchlist.data.StatisticsRepository
import fr.estiam.watchlist.model.CollectionStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Implémentation Room du contrat [StatisticsRepository] (fonctionnalité métier n° 1).
 *
 * combine() assemble 5 flux en un seul objet CollectionStats :
 * dès que l'un d'eux émet une nouvelle valeur (un film ajouté, noté,
 * un statut changé…), les statistiques sont recalculées et l'écran se met à jour.
 */
class RoomStatisticsRepository(database: WatchListDatabase) : StatisticsRepository {

    private val dao = database.movieDao()

    override fun observeStats(): Flow<CollectionStats> = combine(
        dao.observeCount(),
        dao.observeFavoriteCount(),
        dao.observeAverageRating(),
        dao.observeGenreStats(),
        dao.observeStatusStats()
    ) { total, favorites, average, byGenre, byStatus ->
        CollectionStats(
            totalMovies = total,
            favoriteCount = favorites,
            averageRating = average,
            byGenre = byGenre,
            byStatus = byStatus
        )
    }
}
