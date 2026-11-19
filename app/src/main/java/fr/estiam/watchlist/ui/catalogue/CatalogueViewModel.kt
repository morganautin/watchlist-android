package fr.estiam.watchlist.ui.catalogue

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import fr.estiam.watchlist.R
import fr.estiam.watchlist.WatchListApplication
import fr.estiam.watchlist.data.MovieRepository
import fr.estiam.watchlist.data.SettingsRepository
import fr.estiam.watchlist.model.CatalogueQuery
import fr.estiam.watchlist.model.Genre
import fr.estiam.watchlist.model.Movie
import fr.estiam.watchlist.model.MovieSort
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel du catalogue : garde la recherche, le filtre et le tri.
 *
 * Il SURVIT à la rotation de l'écran (contrairement à l'Activity, recréée) :
 * c'est pour ça que les critères ne sont pas perdus quand on tourne le téléphone.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CatalogueViewModel(
    private val repository: MovieRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Texte de recherche, séparé des filtres pour pouvoir lui appliquer un délai (debounce).
    private val searchText = MutableStateFlow("")

    // Filtres propres à cet écran : genre et favoris.
    private val _filters = MutableStateFlow(CatalogueQuery())

    /**
     * Filtres complets = filtres de l'écran + 2 PRÉFÉRENCES DataStore :
     *  - tri par défaut (pref_default_sort) ;
     *  - masquer les films vus (pref_hide_watched).
     * Si une préférence change (ici ou dans « Paramètres »), le catalogue se recharge tout seul.
     * L'écran l'observe aussi pour cocher les bons éléments du menu.
     */
    val filters: StateFlow<CatalogueQuery> =
        combine(_filters, settingsRepository.settings) { filters, prefs ->
            filters.copy(sort = prefs.defaultSort, hideWatched = prefs.hideWatched)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, CatalogueQuery())

    /**
     * Le catalogue paginé (Paging 3).
     * 1. combine : dès que la recherche OU un filtre change, on obtient un nouveau CatalogueQuery ;
     * 2. debounce(300) : on attend que l'utilisateur arrête de taper 0,3 s (évite une requête par lettre) ;
     * 3. flatMapLatest : on crée un nouveau Pager pour ce critère et on abandonne l'ancien ;
     * 4. cachedIn : garde les pages déjà chargées (rotation de l'écran = pas de rechargement).
     */
    val movies: Flow<PagingData<Movie>> =
        combine(searchText.debounce(300), filters) { search, filters -> filters.copy(search = search) }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                Pager(PagingConfig(pageSize = 20, enablePlaceholders = false)) {
                    repository.pagingSource(query)
                }.flow
            }
            .cachedIn(viewModelScope)

    // Messages ponctuels à afficher (Snackbar). Channel = chaque message n'est lu qu'une fois.
    private val _messages = Channel<Int>(Channel.BUFFERED)
    val messages: Flow<Int> = _messages.receiveAsFlow()

    init {
        // Import des films de départ au premier lancement (sans effet ensuite).
        viewModelScope.launch {
            try {
                repository.seedIfNeeded()
            } catch (e: CancellationException) {
                throw e // annulation normale de la coroutine : on la laisse passer
            } catch (e: Exception) {
                Log.e(TAG, "Échec de l'import JSON", e)
                _messages.send(R.string.error_seed)
            }
        }
    }

    fun setSearch(text: String) { searchText.value = text.trim() }

    fun setGenre(genre: Genre?) = _filters.update { it.copy(genre = genre) }

    fun setFavoritesOnly(only: Boolean) = _filters.update { it.copy(favoritesOnly = only) }

    /** Le tri choisi dans le menu est ENREGISTRÉ comme tri par défaut (conservé après relance). */
    fun setSort(sort: MovieSort) {
        viewModelScope.launch { settingsRepository.setDefaultSort(sort) }
    }

    companion object {
        private const val TAG = "CatalogueVM"

        /** Fabrique : explique à Android comment créer ce ViewModel (avec son Repository). */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WatchListApplication
                CatalogueViewModel(app.movies, app.settings)
            }
        }
    }
}
