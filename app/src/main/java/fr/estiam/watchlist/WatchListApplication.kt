package fr.estiam.watchlist

import android.app.Application
import fr.estiam.watchlist.data.MovieRepository
import fr.estiam.watchlist.data.StatisticsRepository
import fr.estiam.watchlist.data.WatchlistRepository
import fr.estiam.watchlist.data.local.RoomMovieRepository
import fr.estiam.watchlist.data.local.RoomStatisticsRepository
import fr.estiam.watchlist.data.local.RoomWatchlistRepository
import fr.estiam.watchlist.data.local.WatchListDatabase

/**
 * Point d'entrée global, créé une seule fois au lancement de l'app.
 * Il fabrique la base et les Repositories, partagés par tous les écrans.
 *
 * Les écrans y accèdent avec : (application as WatchListApplication).movies
 * Les types déclarés sont les INTERFACES : les écrans ne savent pas que c'est Room derrière.
 */
class WatchListApplication : Application() {

    // by lazy : créé à la première utilisation seulement (démarrage plus rapide).
    private val database by lazy { WatchListDatabase.getInstance(this) }

    val movies: MovieRepository by lazy {
        RoomMovieRepository(database) {
            // Lecture du fichier app/src/main/assets/movies.json
            assets.open("movies.json").bufferedReader().use { it.readText() }
        }
    }

    val watchlists: WatchlistRepository by lazy { RoomWatchlistRepository(database) }

    val statistics: StatisticsRepository by lazy { RoomStatisticsRepository(database) }

    // Étape 4 : val settings: SettingsRepository (DataStore)
}
