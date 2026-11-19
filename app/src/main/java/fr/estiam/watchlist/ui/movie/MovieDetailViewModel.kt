package fr.estiam.watchlist.ui.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.estiam.watchlist.WatchListApplication
import fr.estiam.watchlist.data.MovieRepository
import fr.estiam.watchlist.model.Movie
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Les 3 états possibles de l'écran détail. */
sealed interface MovieDetailState {
    data object Loading : MovieDetailState                     // lecture en cours
    data class Loaded(val movie: Movie) : MovieDetailState     // film affiché
    data object NotFound : MovieDetailState                    // film supprimé / inexistant
}

/**
 * ViewModel de l'écran « Détail d'un film ».
 * Il observe le film en continu : après une modification (note, favori,
 * formulaire), l'écran se met à jour tout seul.
 */
class MovieDetailViewModel(
    private val repository: MovieRepository,
    private val movieId: String
) : ViewModel() {

    /**
     * stateIn transforme le Flow en StateFlow : il garde la dernière valeur,
     * que l'écran retrouve immédiatement après une rotation.
     */
    val state: StateFlow<MovieDetailState> = repository.observeMovie(movieId)
        .map { movie -> if (movie == null) MovieDetailState.NotFound else MovieDetailState.Loaded(movie) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MovieDetailState.Loading)

    /** Inverse le statut favori du film affiché. */
    fun toggleFavorite() {
        val movie = (state.value as? MovieDetailState.Loaded)?.movie ?: return
        viewModelScope.launch { repository.setFavorite(movie.id, !movie.isFavorite) }
    }

    /** Note de 1 à 5, ou null pour retirer la note. */
    fun setRating(rating: Int?) {
        viewModelScope.launch { repository.setRating(movieId, rating) }
    }

    /** Supprime le film ; l'état passe alors à NotFound et l'écran se ferme. */
    fun delete() {
        viewModelScope.launch { repository.deleteMovie(movieId) }
    }

    companion object {
        const val EXTRA_MOVIE_ID = "movie_id"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WatchListApplication
                // SavedStateHandle contient automatiquement les "extras" de l'Intent qui a ouvert l'écran.
                val movieId = createSavedStateHandle().get<String>(EXTRA_MOVIE_ID)
                    ?: error("MovieDetailActivity doit recevoir un id de film")
                MovieDetailViewModel(app.movies, movieId)
            }
        }
    }
}
