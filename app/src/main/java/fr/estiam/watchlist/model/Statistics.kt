package fr.estiam.watchlist.model

/**
 * Fonctionnalité métier n° 1 : statistiques de la collection.
 * Calculées à partir des tables (pas de valeurs écrites en dur) et
 * recalculées automatiquement à chaque modification (Flow).
 */
data class CollectionStats(
    val totalMovies: Int = 0,
    val favoriteCount: Int = 0,
    val averageRating: Double? = null,       // null si aucun film noté
    val byGenre: List<GenreStat> = emptyList(),
    val byStatus: List<StatusStat> = emptyList()
)

/** Nombre de films et note moyenne pour un genre. */
data class GenreStat(
    val genre: Genre,
    val count: Int,
    val averageRating: Double?
)

/** Nombre de films (dans toutes les listes) pour un statut. */
data class StatusStat(
    val status: WatchStatus,
    val count: Int
)
