package fr.estiam.watchlist.ui.movie

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.estiam.watchlist.WatchListApplication
import fr.estiam.watchlist.data.MovieRepository
import fr.estiam.watchlist.model.Genre
import fr.estiam.watchlist.model.Movie
import fr.estiam.watchlist.model.MovieError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

/** Ce que l'utilisateur a tapé dans le formulaire (tout en texte, brut). */
data class MovieFormInput(
    val title: String,
    val year: String,
    val duration: String,
    val director: String,
    val synopsis: String,
    val genre: Genre?
)

/** Événements ponctuels envoyés à l'écran. */
sealed interface MovieFormEvent {
    /** Saisie invalide : erreurs à afficher sous les champs. */
    data class Invalid(val errors: Set<MovieError>, val genreMissing: Boolean) : MovieFormEvent
    data object Saved : MovieFormEvent
    data object SaveFailed : MovieFormEvent
}

/**
 * ViewModel du formulaire « Ajouter / Modifier un film » (CRUD n° 1).
 *
 * @param movieId null = ajout d'un nouveau film ; sinon = modification de ce film.
 */
class MovieFormViewModel(
    private val repository: MovieRepository,
    private val movieId: String?
) : ViewModel() {

    val isEditMode: Boolean = movieId != null

    // Film d'origine (mode modification), chargé une seule fois.
    private val _original = MutableStateFlow<Movie?>(null)
    val original: StateFlow<Movie?> = _original.asStateFlow()

    /**
     * Vrai quand les champs ont déjà été remplis avec le film d'origine.
     * Le ViewModel survit à la rotation : on ne re-remplit pas les champs
     * (ce qui effacerait ce que l'utilisateur est en train de taper).
     */
    var fieldsFilled = false

    /** Genre choisi dans la liste déroulante (gardé ici pour survivre à la rotation). */
    var selectedGenre: Genre? = null

    private val _events = Channel<MovieFormEvent>(Channel.BUFFERED)
    val events: Flow<MovieFormEvent> = _events.receiveAsFlow()

    init {
        if (movieId != null) {
            viewModelScope.launch {
                val movie = repository.getMovie(movieId)
                if (selectedGenre == null) selectedGenre = movie?.genre
                _original.value = movie
            }
        }
    }

    /** Bouton « Enregistrer » : valide, puis enregistre si tout est correct. */
    fun save(input: MovieFormInput) {
        val movie = buildMovie(input, _original.value)
        val errors = movie.validate()
        val genreMissing = input.genre == null
        if (errors.isNotEmpty() || genreMissing) {
            viewModelScope.launch { _events.send(MovieFormEvent.Invalid(errors, genreMissing)) }
            return
        }
        viewModelScope.launch {
            try {
                repository.saveMovie(movie)
                _events.send(MovieFormEvent.Saved)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MovieForm", "Échec de l'enregistrement", e)
                _events.send(MovieFormEvent.SaveFailed)
            }
        }
    }

    companion object {
        /**
         * Transforme la saisie en Movie. Fonction "pure" (aucun accès à la base),
         * donc testable avec JUnit.
         * - Un nombre mal tapé devient -1 : validate() le signalera comme invalide.
         * - En modification, on garde l'id, la note et le favori du film d'origine.
         */
        fun buildMovie(input: MovieFormInput, original: Movie?): Movie {
            val base = original ?: Movie(
                id = UUID.randomUUID().toString(), title = "", year = 0,
                genre = input.genre ?: Genre.DRAMA, durationMinutes = 0
            )
            return base.copy(
                title = input.title.trim(),
                year = input.year.trim().toIntOrNull() ?: -1,
                durationMinutes = input.duration.trim().toIntOrNull() ?: -1,
                director = input.director.trim(),
                synopsis = input.synopsis.trim(),
                genre = input.genre ?: base.genre
            )
        }

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WatchListApplication
                // Absent de l'Intent = mode ajout.
                val movieId = createSavedStateHandle().get<String>(MovieDetailViewModel.EXTRA_MOVIE_ID)
                MovieFormViewModel(app.movies, movieId)
            }
        }
    }
}
