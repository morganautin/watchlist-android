package fr.estiam.watchlist.ui.watchlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.estiam.watchlist.WatchListApplication
import fr.estiam.watchlist.data.WatchlistRepository
import fr.estiam.watchlist.model.WatchlistSummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Événements ponctuels de l'écran « Mes listes ». */
sealed interface WatchlistsEvent {
    data class Created(val name: String) : WatchlistsEvent
    data object Failed : WatchlistsEvent
}

/**
 * ViewModel de l'écran « Mes listes » (CRUD n° 2 : création + lecture).
 * La modification et la suppression se font depuis le détail d'une liste.
 */
class WatchlistsViewModel(private val repository: WatchlistRepository) : ViewModel() {

    /**
     * Toutes les listes avec leur progression.
     * null = pas encore chargé (évite d'afficher « aucune liste » une fraction de seconde).
     */
    val summaries: StateFlow<List<WatchlistSummary>?> = repository.observeSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _events = Channel<WatchlistsEvent>(Channel.BUFFERED)
    val events: Flow<WatchlistsEvent> = _events.receiveAsFlow()

    /** Création d'une liste (la saisie a déjà été vérifiée par la boîte de dialogue). */
    fun create(name: String, description: String) {
        viewModelScope.launch {
            try {
                repository.createWatchlist(name, description)
                _events.send(WatchlistsEvent.Created(name))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("WatchlistsVM", "Échec de la création de liste", e)
                _events.send(WatchlistsEvent.Failed)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WatchListApplication
                WatchlistsViewModel(app.watchlists)
            }
        }
    }
}
