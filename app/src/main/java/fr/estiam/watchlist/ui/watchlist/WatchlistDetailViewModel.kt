package fr.estiam.watchlist.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.estiam.watchlist.WatchListApplication
import fr.estiam.watchlist.data.WatchlistRepository
import fr.estiam.watchlist.model.WatchStatus
import fr.estiam.watchlist.model.Watchlist
import fr.estiam.watchlist.model.WatchlistEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** États de l'écran « Détail d'une liste ». */
sealed interface WatchlistDetailState {
    data object Loading : WatchlistDetailState
    data class Loaded(val watchlist: Watchlist, val entries: List<WatchlistEntry>) : WatchlistDetailState {
        // Progression recalculée à partir des films affichés (fonctionnalité métier n° 2)
        val watchedCount: Int get() = entries.count { it.status == WatchStatus.WATCHED }
        val progressPercent: Int get() = if (entries.isEmpty()) 0 else watchedCount * 100 / entries.size
    }
    data object NotFound : WatchlistDetailState
}

/**
 * ViewModel du détail d'une liste : films de la liste, changement de statut,
 * retrait d'un film, modification et suppression de la liste (CRUD n° 2).
 */
class WatchlistDetailViewModel(
    private val repository: WatchlistRepository,
    private val watchlistId: Long
) : ViewModel() {

    /**
     * combine : la liste ET ses films, observés ensemble.
     * Changer un statut met à jour la base → Room ré-émet → l'écran
     * (et la progression) se mettent à jour sans rafraîchissement manuel.
     */
    val state: StateFlow<WatchlistDetailState> = combine(
        repository.observeWatchlist(watchlistId),
        repository.observeEntries(watchlistId)
    ) { watchlist, entries ->
        if (watchlist == null) WatchlistDetailState.NotFound
        else WatchlistDetailState.Loaded(watchlist, entries)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WatchlistDetailState.Loading)

    fun setStatus(movieId: String, status: WatchStatus) {
        viewModelScope.launch { repository.setStatus(watchlistId, movieId, status) }
    }

    fun removeMovie(movieId: String) {
        viewModelScope.launch { repository.removeMovie(watchlistId, movieId) }
    }

    fun update(name: String, description: String) {
        val current = (state.value as? WatchlistDetailState.Loaded)?.watchlist ?: return
        viewModelScope.launch { repository.updateWatchlist(current.copy(name = name, description = description)) }
    }

    /** Supprime la liste ; l'état passe à NotFound et l'écran se ferme. */
    fun delete() {
        viewModelScope.launch { repository.deleteWatchlist(watchlistId) }
    }

    companion object {
        const val EXTRA_WATCHLIST_ID = "watchlist_id"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WatchListApplication
                val id = createSavedStateHandle().get<Long>(EXTRA_WATCHLIST_ID)
                    ?: error("WatchlistDetailActivity doit recevoir un id de liste")
                WatchlistDetailViewModel(app.watchlists, id)
            }
        }
    }
}
