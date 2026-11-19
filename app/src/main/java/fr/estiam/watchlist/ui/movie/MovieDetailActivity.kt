package fr.estiam.watchlist.ui.movie

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.estiam.watchlist.R
import fr.estiam.watchlist.databinding.ActivityMovieDetailBinding
import fr.estiam.watchlist.model.Movie
import fr.estiam.watchlist.ui.common.applySystemBarInsets
import fr.estiam.watchlist.ui.common.color
import fr.estiam.watchlist.ui.common.enableCinemaEdgeToEdge
import fr.estiam.watchlist.ui.common.formatDuration
import fr.estiam.watchlist.ui.common.labelRes
import fr.estiam.watchlist.ui.common.showWatchlistDialog
import kotlinx.coroutines.launch

/**
 * ÉCRAN 2 — Détail d'un film.
 * Affiche le film, permet de le noter, de le mettre en favori,
 * de le modifier (formulaire) ou de le supprimer.
 */
class MovieDetailActivity : AppCompatActivity() {

    private lateinit var b: ActivityMovieDetailBinding
    private val vm: MovieDetailViewModel by viewModels { MovieDetailViewModel.Factory }

    // Titre du film affiché, pour le message après suppression.
    private var displayedTitle: String = ""
    // Vrai quand l'utilisateur a demandé la suppression (pour afficher le bon message).
    private var deleteRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableCinemaEdgeToEdge()
        super.onCreate(savedInstanceState)
        b = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)   // flèche "retour"
        applySystemBarInsets(b.toolbar, b.root)

        // Note : fromUser = true seulement si c'est l'utilisateur qui a touché les étoiles
        // (pas quand le code affiche la note enregistrée).
        b.ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) vm.setRating(rating.toInt().takeIf { it in 1..5 })
        }
        b.clearRatingButton.setOnClickListener { vm.setRating(null) }
        b.favoriteButton.setOnClickListener { vm.toggleFavorite() }
        b.editButton.setOnClickListener {
            val id = intent.getStringExtra(MovieDetailViewModel.EXTRA_MOVIE_ID) ?: return@setOnClickListener
            startActivity(MovieFormActivity.intent(this, id))
        }
        b.deleteButton.setOnClickListener { confirmDelete() }
        b.addToListButton.setOnClickListener { chooseLists() }

        // Observation de l'état : l'écran se redessine à chaque changement en base.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Texte « Dans 2 listes », mis à jour à chaque ajout / retrait
                launch {
                    vm.memberListIds.collect { ids ->
                        b.listsText.text = if (ids.isEmpty()) getString(R.string.in_no_list)
                        else resources.getQuantityString(R.plurals.in_lists, ids.size, ids.size)
                    }
                }
                vm.state.collect { state ->
                    when (state) {
                        MovieDetailState.Loading -> b.content.isVisible = false
                        is MovieDetailState.Loaded -> show(state.movie)
                        MovieDetailState.NotFound -> {
                            // Film supprimé (par nous ou introuvable) : on ferme l'écran.
                            val message = if (deleteRequested) getString(R.string.movie_deleted, displayedTitle)
                            else getString(R.string.movie_not_found)
                            Toast.makeText(this@MovieDetailActivity, message, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        }
    }

    /** Remplit l'écran avec les données du film. */
    private fun show(movie: Movie) {
        displayedTitle = movie.title
        b.content.isVisible = true
        b.posterBand.setBackgroundColor(movie.genre.color())
        b.posterBand.text = movie.title.firstOrNull()?.uppercase() ?: "?"
        b.titleText.text = movie.title
        b.metaText.text = getString(
            R.string.movie_meta, movie.year, getString(movie.genre.labelRes()), formatDuration(movie.durationMinutes)
        )
        b.directorText.isVisible = movie.director.isNotBlank()
        b.directorText.text = getString(R.string.director_label, movie.director)
        b.ratingBar.rating = (movie.rating ?: 0).toFloat()
        b.clearRatingButton.isVisible = movie.rating != null
        b.favoriteButton.setText(if (movie.isFavorite) R.string.favorite_remove else R.string.favorite_add)
        b.synopsisText.text = movie.synopsis.ifBlank { getString(R.string.no_synopsis) }
    }

    /**
     * Boîte « Dans quelles listes ? » : une case à cocher par liste.
     * Cocher ajoute le film à la liste, décocher l'en retire (immédiatement).
     */
    private fun chooseLists() {
        val lists = vm.watchlists.value
        // Trace Logcat (filtre : MovieDetail) — sert au bug n° 1 de docs/DEBUGGING.md
        Log.d("MovieDetail", "Ajouter à une liste : ${lists.size} liste(s) disponible(s)")
        if (lists.isEmpty()) {
            // Aucune liste : on propose directement d'en créer une.
            showWatchlistDialog(R.string.new_list) { name, description -> vm.createListAndAdd(name, description) }
            return
        }
        val names = lists.map { it.watchlist.name }.toTypedArray<CharSequence>()
        val members = vm.memberListIds.value
        val checked = BooleanArray(lists.size) { lists[it].watchlist.id in members }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.choose_lists)
            .setMultiChoiceItems(names, checked) { _, which, isChecked ->
                vm.setInList(lists[which].watchlist.id, isChecked)
            }
            .setNeutralButton(R.string.new_list_ellipsis) { _, _ ->
                showWatchlistDialog(R.string.new_list) { name, description -> vm.createListAndAdd(name, description) }
            }
            .setPositiveButton(R.string.done, null)
            .show()
    }

    /** Boîte de confirmation avant une action irréversible. */
    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(getString(R.string.delete_confirm_message, displayedTitle))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteRequested = true
                vm.delete()
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        /** Construit l'Intent qui ouvre cet écran pour un film donné. */
        fun intent(context: Context, movieId: String): Intent =
            Intent(context, MovieDetailActivity::class.java)
                .putExtra(MovieDetailViewModel.EXTRA_MOVIE_ID, movieId)
    }
}
