package fr.estiam.watchlist.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.estiam.watchlist.WatchListApplication
import fr.estiam.watchlist.data.StatisticsRepository
import fr.estiam.watchlist.model.CollectionStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel de l'écran « Statistiques » (fonctionnalité métier n° 1).
 * Les chiffres sont calculés par Room et recalculés à chaque modification.
 */
class StatisticsViewModel(repository: StatisticsRepository) : ViewModel() {

    /** null = pas encore chargé. */
    val stats: StateFlow<CollectionStats?> = repository.observeStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WatchListApplication
                StatisticsViewModel(app.statistics)
            }
        }
    }
}
