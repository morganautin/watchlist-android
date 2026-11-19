package fr.estiam.watchlist.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import fr.estiam.watchlist.R
import fr.estiam.watchlist.WatchListApplication
import fr.estiam.watchlist.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/**
 * Écran provisoire (étape 2) : vérifie que la base Room et l'import JSON fonctionnent.
 * Il deviendra le catalogue des films à l'étape 3.
 */
class MainActivity : AppCompatActivity() {

    // Accès aux vues du fichier activity_main.xml (généré par ViewBinding)
    private lateinit var b: ActivityMainBinding

    // Raccourci vers les Repositories créés dans WatchListApplication
    private val app get() = application as WatchListApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. On "gonfle" le layout XML et on l'affiche
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)

        // 2. Coroutine liée au cycle de vie de l'écran : elle s'arrête quand
        //    l'écran passe en arrière-plan et reprend quand il revient (STARTED).
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 2a. Import du JSON (ne fait rien si déjà fait).
                launch { app.movies.seedIfNeeded() }

                // 2b. Observation réactive des statistiques : le texte se met à jour
                //     tout seul quand l'import ajoute les films en base.
                launch {
                    app.statistics.observeStats().collect { stats ->
                        b.welcomeText.text = getString(
                            R.string.database_ready, stats.totalMovies, stats.favoriteCount
                        )
                    }
                }
            }
        }
    }
}
