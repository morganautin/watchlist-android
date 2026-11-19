package fr.estiam.watchlist.ui.movie

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import fr.estiam.watchlist.R
import fr.estiam.watchlist.databinding.ActivityMovieFormBinding
import fr.estiam.watchlist.model.Genre
import fr.estiam.watchlist.model.Movie
import fr.estiam.watchlist.model.MovieError
import fr.estiam.watchlist.ui.common.applySystemBarInsets
import fr.estiam.watchlist.ui.common.enableCinemaEdgeToEdge
import fr.estiam.watchlist.ui.common.labelRes
import fr.estiam.watchlist.ui.common.messageRes
import kotlinx.coroutines.launch

/**
 * ÉCRAN 3 — Formulaire « Ajouter / Modifier un film ».
 * Contrôles de saisie : chaque erreur s'affiche sous le champ concerné.
 */
class MovieFormActivity : AppCompatActivity() {

    private lateinit var b: ActivityMovieFormBinding
    private val vm: MovieFormViewModel by viewModels { MovieFormViewModel.Factory }

    // Libellés des genres, dans l'ordre de l'enum (position i ↔ Genre.entries[i])
    private val genreLabels by lazy { Genre.entries.map { getString(it.labelRes()) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableCinemaEdgeToEdge()
        super.onCreate(savedInstanceState)
        b = ActivityMovieFormBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(if (vm.isEditMode) R.string.form_edit_title else R.string.form_add_title)
        applySystemBarInsets(b.toolbar, b.root)

        setupGenreDropdown()
        clearErrorWhenTyping()
        b.saveButton.setOnClickListener { vm.save(readInput()) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Mode modification : remplir les champs UNE seule fois quand le film est chargé.
                launch {
                    vm.original.collect { movie ->
                        if (movie != null && !vm.fieldsFilled) {
                            fillFields(movie)
                            vm.fieldsFilled = true
                        }
                    }
                }
                // Résultat de l'enregistrement
                launch {
                    vm.events.collect { event ->
                        when (event) {
                            is MovieFormEvent.Invalid -> showErrors(event.errors, event.genreMissing)
                            MovieFormEvent.Saved -> {
                                Toast.makeText(this@MovieFormActivity, R.string.movie_saved, Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            MovieFormEvent.SaveFailed ->
                                Snackbar.make(b.root, R.string.save_failed, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    /** Liste déroulante des genres. */
    private fun setupGenreDropdown() {
        // On ne laisse pas Android restaurer ce champ : le genre choisi est dans le ViewModel.
        b.genreInput.isSaveEnabled = false
        b.genreInput.setSimpleItems(genreLabels.toTypedArray())
        vm.selectedGenre?.let { b.genreInput.setText(genreLabels[it.ordinal], false) }
        b.genreInput.setOnItemClickListener { _, _, position, _ ->
            vm.selectedGenre = Genre.entries[position]
            b.genreLayout.error = null
        }
    }

    /** Copie le film d'origine dans les champs (mode modification). */
    private fun fillFields(movie: Movie) {
        b.titleInput.setText(movie.title)
        b.yearInput.setText(movie.year.toString())
        b.durationInput.setText(movie.durationMinutes.toString())
        b.directorInput.setText(movie.director)
        b.synopsisInput.setText(movie.synopsis)
        b.genreInput.setText(genreLabels[movie.genre.ordinal], false)
    }

    /** Lit le contenu brut des champs. */
    private fun readInput() = MovieFormInput(
        title = b.titleInput.text?.toString().orEmpty(),
        year = b.yearInput.text?.toString().orEmpty(),
        duration = b.durationInput.text?.toString().orEmpty(),
        director = b.directorInput.text?.toString().orEmpty(),
        synopsis = b.synopsisInput.text?.toString().orEmpty(),
        genre = vm.selectedGenre
    )

    /** Affiche chaque erreur sous le bon champ (ou l'efface s'il est correct). */
    private fun showErrors(errors: Set<MovieError>, genreMissing: Boolean) {
        fun errorFor(vararg candidates: MovieError): String? =
            candidates.firstOrNull { it in errors }?.let { getString(it.messageRes()) }

        b.titleLayout.error = errorFor(MovieError.EMPTY_TITLE, MovieError.TITLE_TOO_LONG)
        b.yearLayout.error = errorFor(MovieError.INVALID_YEAR)
        b.durationLayout.error = errorFor(MovieError.INVALID_DURATION)
        b.genreLayout.error = if (genreMissing) getString(R.string.error_genre) else null
    }

    /** Dès que l'utilisateur corrige un champ, son message d'erreur disparaît. */
    private fun clearErrorWhenTyping() {
        listOf<Pair<TextInputLayout, TextInputEditText>>(
            b.titleLayout to b.titleInput,
            b.yearLayout to b.yearInput,
            b.durationLayout to b.durationInput
        ).forEach { (layout, input) -> input.doAfterTextChanged { layout.error = null } }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        /** Intent pour MODIFIER un film existant (sans id = ajout). */
        fun intent(context: Context, movieId: String): Intent =
            Intent(context, MovieFormActivity::class.java)
                .putExtra(MovieDetailViewModel.EXTRA_MOVIE_ID, movieId)
    }
}
