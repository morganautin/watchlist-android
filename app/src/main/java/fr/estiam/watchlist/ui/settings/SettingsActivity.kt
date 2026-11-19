package fr.estiam.watchlist.ui.settings

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import fr.estiam.watchlist.R
import fr.estiam.watchlist.databinding.ActivitySettingsBinding
import fr.estiam.watchlist.model.MovieSort
import fr.estiam.watchlist.model.ThemeMode
import fr.estiam.watchlist.model.UserSettings
import fr.estiam.watchlist.ui.common.applySystemBarInsets
import fr.estiam.watchlist.ui.common.enableCinemaEdgeToEdge
import kotlinx.coroutines.launch

/**
 * ÉCRAN 7 — Paramètres.
 * Chaque choix est enregistré immédiatement dans DataStore (pas de bouton « Enregistrer »)
 * et conservé après fermeture de l'app.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var b: ActivitySettingsBinding
    private val vm: SettingsViewModel by viewModels { SettingsViewModel.Factory }

    // Vrai pendant que le CODE coche les boutons : on ignore alors les écouteurs
    // (sinon afficher une valeur la ré-enregistrerait en boucle).
    private var updatingUi = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableCinemaEdgeToEdge()
        super.onCreate(savedInstanceState)
        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applySystemBarInsets(b.toolbar, b.root)

        // 1. Thème : le changement s'applique à toute l'app (voir WatchListApplication).
        b.themeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (updatingUi || !isChecked) return@addOnButtonCheckedListener
            vm.setTheme(
                when (checkedId) {
                    R.id.themeLight -> ThemeMode.LIGHT
                    R.id.themeDark -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                }
            )
        }

        // 2. Tri par défaut du catalogue
        b.sortGroup.setOnCheckedChangeListener { _, checkedId ->
            if (updatingUi) return@setOnCheckedChangeListener
            vm.setDefaultSort(
                when (checkedId) {
                    R.id.sortYear -> MovieSort.YEAR
                    R.id.sortRating -> MovieSort.RATING
                    else -> MovieSort.TITLE
                }
            )
        }

        // 3. Masquer les films vus
        b.hideWatchedSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!updatingUi) vm.setHideWatched(isChecked)
        }

        // L'écran affiche toujours ce qui est enregistré dans DataStore.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.settings.collect { settings -> if (settings != null) show(settings) }
            }
        }
    }

    private fun show(settings: UserSettings) {
        updatingUi = true
        b.themeGroup.check(
            when (settings.theme) {
                ThemeMode.SYSTEM -> R.id.themeSystem
                ThemeMode.LIGHT -> R.id.themeLight
                ThemeMode.DARK -> R.id.themeDark
            }
        )
        b.sortGroup.check(
            when (settings.defaultSort) {
                MovieSort.TITLE -> R.id.sortTitle
                MovieSort.YEAR -> R.id.sortYear
                MovieSort.RATING -> R.id.sortRating
            }
        )
        b.hideWatchedSwitch.isChecked = settings.hideWatched
        updatingUi = false
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
