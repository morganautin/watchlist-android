package fr.estiam.watchlist

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import fr.estiam.watchlist.data.MovieRepository
import fr.estiam.watchlist.data.SettingsRepository
import fr.estiam.watchlist.data.StatisticsRepository
import fr.estiam.watchlist.data.WatchlistRepository
import fr.estiam.watchlist.data.local.RoomMovieRepository
import fr.estiam.watchlist.data.local.RoomStatisticsRepository
import fr.estiam.watchlist.data.local.RoomWatchlistRepository
import fr.estiam.watchlist.data.local.WatchListDatabase
import fr.estiam.watchlist.data.prefs.DataStoreSettingsRepository
import fr.estiam.watchlist.data.prefs.settingsDataStore
import fr.estiam.watchlist.model.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Point d'entrée global, créé une seule fois au lancement de l'app.
 * Il fabrique la base, les Repositories et le DataStore, partagés par tous les écrans.
 *
 * Les écrans y accèdent avec : (application as WatchListApplication).movies
 * Les types déclarés sont les INTERFACES : les écrans ne savent pas que c'est Room / DataStore derrière.
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

    val settings: SettingsRepository by lazy { DataStoreSettingsRepository(settingsDataStore) }

    // Coroutines qui vivent aussi longtemps que l'application.
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()

        // PRÉFÉRENCE « THÈME » (effet visible sur toute l'app)
        // 1. Au démarrage : on lit le thème enregistré AVANT d'afficher le premier écran,
        //    sinon l'app s'ouvrirait en clair puis basculerait en sombre (clignotement).
        //    runBlocking est acceptable ici : lecture d'un tout petit fichier, une seule fois.
        val initialTheme = runBlocking { settings.settings.first().theme }
        applyTheme(initialTheme)

        // 2. Ensuite : à chaque changement dans « Paramètres », on applique le nouveau thème.
        //    AppCompat recrée alors les écrans ouverts → mise à jour sans action de l'utilisateur.
        appScope.launch {
            settings.settings.map { it.theme }.distinctUntilChanged().collect { applyTheme(it) }
        }
    }

    private fun applyTheme(theme: ThemeMode) {
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            }
        )
    }
}
