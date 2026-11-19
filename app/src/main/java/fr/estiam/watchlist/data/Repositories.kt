package fr.estiam.watchlist.data

import androidx.paging.PagingSource
import fr.estiam.watchlist.model.CatalogueQuery
import fr.estiam.watchlist.model.CollectionStats
import fr.estiam.watchlist.model.Movie
import fr.estiam.watchlist.model.MovieSort
import fr.estiam.watchlist.model.ThemeMode
import fr.estiam.watchlist.model.UserSettings
import fr.estiam.watchlist.model.WatchStatus
import fr.estiam.watchlist.model.Watchlist
import fr.estiam.watchlist.model.WatchlistEntry
import fr.estiam.watchlist.model.WatchlistSummary
import kotlinx.coroutines.flow.Flow

/*
 * CONTRATS DES REPOSITORIES
 * ------------------------
 * Une interface = la liste des fonctions qu'un écran a le droit d'appeler.
 * Les ViewModels ne connaissent QUE ces interfaces, jamais Room directement
 * (« un écran dépend du contrat du repository, pas d'une classe de base de données »).
 *
 * Règles communes :
 *  - fun ...(): Flow<...>  → lecture réactive : l'écran se met à jour tout seul
 *    quand la base change, sans rafraîchissement manuel ;
 *  - suspend fun ...       → écriture/lecture ponctuelle, exécutée hors du
 *    thread principal (à appeler depuis viewModelScope.launch { }).
 *
 * L'implémentation Room de chaque interface est écrite à l'étape 2.
 */

/** CRUD n° 1 — Le catalogue de films. */
interface MovieRepository {
    /** Catalogue paginé (Paging 3) avec recherche, filtres et tri. */
    fun pagingSource(query: CatalogueQuery): PagingSource<Int, Movie>

    /** Un film observé en continu (écran détail) ; émet null s'il est supprimé. */
    fun observeMovie(id: String): Flow<Movie?>

    /** Lecture ponctuelle (ex. pré-remplir le formulaire de modification). */
    suspend fun getMovie(id: String): Movie?

    /** Ajout OU modification selon l'id. Lève IllegalArgumentException si le film est invalide. */
    suspend fun saveMovie(movie: Movie)

    /** Suppression ; le film disparaît aussi de toutes les listes (CASCADE). */
    suspend fun deleteMovie(id: String)

    suspend fun setFavorite(id: String, favorite: Boolean)

    /** Note de 1 à 5, ou null pour retirer la note. */
    suspend fun setRating(id: String, rating: Int?)

    /** Import de assets/movies.json une seule fois, sans doublons ni écrasement. */
    suspend fun seedIfNeeded()
}

/** CRUD n° 2 — Les listes et leur contenu. */
interface WatchlistRepository {
    /** Toutes les listes avec leur progression (écran « Mes listes »). */
    fun observeSummaries(): Flow<List<WatchlistSummary>>

    fun observeWatchlist(id: Long): Flow<Watchlist?>

    /** Films d'une liste avec leur statut (écran « Détail d'une liste »). */
    fun observeEntries(watchlistId: Long): Flow<List<WatchlistEntry>>

    /** Ids des listes qui contiennent déjà ce film (pour cocher les cases sur le détail film). */
    fun observeWatchlistIdsForMovie(movieId: String): Flow<List<Long>>

    /** Crée une liste et renvoie son id. Lève IllegalArgumentException si le nom est invalide. */
    suspend fun createWatchlist(name: String, description: String): Long

    suspend fun updateWatchlist(watchlist: Watchlist)

    /** Supprime la liste et ses lignes (les films restent dans le catalogue). */
    suspend fun deleteWatchlist(id: Long)

    /** Ajoute un film (statut « à voir ») ; sans effet s'il y est déjà. */
    suspend fun addMovie(watchlistId: Long, movieId: String)

    suspend fun removeMovie(watchlistId: Long, movieId: String)

    suspend fun setStatus(watchlistId: Long, movieId: String, status: WatchStatus)
}

/** Fonctionnalité métier n° 1 — Statistiques (écran « Statistiques »). */
interface StatisticsRepository {
    /** Recalculées automatiquement à chaque ajout, modification ou suppression. */
    fun observeStats(): Flow<CollectionStats>
}

/** Préférences persistantes (DataStore) — écran « Paramètres ». */
interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setTheme(theme: ThemeMode)
    suspend fun setDefaultSort(sort: MovieSort)
    suspend fun setHideWatched(hide: Boolean)
}
