package fr.estiam.watchlist.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.estiam.watchlist.WatchListApplication
import fr.estiam.watchlist.data.SettingsRepository
import fr.estiam.watchlist.model.MovieSort
import fr.estiam.watchlist.model.ThemeMode
import fr.estiam.watchlist.model.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel de l'écran « Paramètres » : lit et écrit les 3 préférences DataStore.
 */
class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    /** null = pas encore lu sur le disque. */
    val settings: StateFlow<UserSettings?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setTheme(theme: ThemeMode) { viewModelScope.launch { repository.setTheme(theme) } }

    fun setDefaultSort(sort: MovieSort) { viewModelScope.launch { repository.setDefaultSort(sort) } }

    fun setHideWatched(hide: Boolean) { viewModelScope.launch { repository.setHideWatched(hide) } }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WatchListApplication
                SettingsViewModel(app.settings)
            }
        }
    }
}
