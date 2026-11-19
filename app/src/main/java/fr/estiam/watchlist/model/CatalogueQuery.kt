package fr.estiam.watchlist.model

/**
 * Tous les critères du catalogue réunis en un seul objet :
 * recherche + filtres + tri. Le ViewModel du catalogue en garde un
 * dans son état ; à chaque changement, Paging 3 recharge la liste.
 */
data class CatalogueQuery(
    val search: String = "",          // texte tapé dans la barre de recherche (titre ou réalisateur)
    val genre: Genre? = null,         // null = tous les genres
    val favoritesOnly: Boolean = false,
    val hideWatched: Boolean = false, // préférence : masquer les films vus dans au moins une liste
    val sort: MovieSort = MovieSort.TITLE
)

/** Ordres de tri du catalogue (aussi utilisé par la préférence « tri par défaut »). */
enum class MovieSort { TITLE, YEAR, RATING }
