package fr.estiam.watchlist.ui.catalogue

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import fr.estiam.watchlist.R
import fr.estiam.watchlist.databinding.ActivityCatalogueBinding
import fr.estiam.watchlist.model.Genre
import fr.estiam.watchlist.model.MovieSort
import fr.estiam.watchlist.ui.common.applySystemBarInsets
import fr.estiam.watchlist.ui.common.enableCinemaEdgeToEdge
import fr.estiam.watchlist.ui.common.labelRes
import fr.estiam.watchlist.ui.movie.MovieDetailActivity
import fr.estiam.watchlist.ui.movie.MovieFormActivity
import fr.estiam.watchlist.ui.settings.SettingsActivity
import fr.estiam.watchlist.ui.stats.StatisticsActivity
import fr.estiam.watchlist.ui.watchlist.WatchlistsActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ÉCRAN 1 — Catalogue (écran de démarrage).
 * Liste paginée + recherche + filtre par genre + favoris + tri.
 *
 * L'Activity ne fait QUE de l'affichage : tout l'état est dans CatalogueViewModel.
 */
class CatalogueActivity : AppCompatActivity() {

    private lateinit var b: ActivityCatalogueBinding

    // "by viewModels" : Android crée le ViewModel une seule fois et le réutilise après rotation.
    private val vm: CatalogueViewModel by viewModels { CatalogueViewModel.Factory }

    // Toucher un film ouvre son détail en lui passant l'id du film.
    private val adapter = MovieAdapter { movie ->
        startActivity(MovieDetailActivity.intent(this, movie.id))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableCinemaEdgeToEdge()
        super.onCreate(savedInstanceState)
        b = ActivityCatalogueBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        applySystemBarInsets(b.toolbar, b.root)

        // 1. Liste
        b.movieList.layoutManager = LinearLayoutManager(this)
        b.movieList.adapter = adapter
        b.retryButton.setOnClickListener { adapter.retry() }

        // 2. Recherche : chaque frappe est transmise au ViewModel (qui attend 0,3 s).
        b.searchInput.doAfterTextChanged { text -> vm.setSearch(text?.toString().orEmpty()) }

        // 3. Filtres par genre
        setupGenreChips()

        // 4. Bouton d'ajout : formulaire vide
        b.addButton.setOnClickListener { startActivity(Intent(this, MovieFormActivity::class.java)) }

        // 5. Observation des données, uniquement quand l'écran est visible (STARTED).
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 5a. Les pages de films → l'adapter
                launch { vm.movies.collectLatest { adapter.submitData(it) } }

                // 5b. État du chargement → chargement / liste vide / erreur
                launch {
                    adapter.loadStateFlow.collect { state ->
                        val refresh = state.refresh
                        val isEmpty = refresh is LoadState.NotLoading && adapter.itemCount == 0
                        b.progress.isVisible = refresh is LoadState.Loading && adapter.itemCount == 0
                        b.errorPanel.isVisible = refresh is LoadState.Error
                        b.emptyText.isVisible = isEmpty
                        b.movieList.isVisible = refresh !is LoadState.Error
                    }
                }

                // 5c. Filtres modifiés → mettre à jour le menu (cases cochées) et le sous-titre
                launch {
                    vm.filters.collect { filters ->
                        invalidateOptionsMenu()
                        // Sous-titre : rappelle les filtres actifs (dont la préférence « masquer les vus »)
                        supportActionBar?.subtitle = listOfNotNull(
                            getString(R.string.favorites_only).takeIf { filters.favoritesOnly },
                            getString(R.string.hidden_watched_subtitle).takeIf { filters.hideWatched }
                        ).joinToString(" · ").ifEmpty { null }
                    }
                }

                // 5d. Messages ponctuels (ex. échec de l'import)
                launch {
                    vm.messages.collect { message ->
                        Snackbar.make(b.root, message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    /** Crée une chip "Tous" puis une chip par genre. */
    private fun setupGenreChips() {
        val group = b.genreChips
        // Les chips sont recréées à chaque rotation : on ne laisse pas Android
        // restaurer un ancien état, c'est le ViewModel qui fait foi.
        group.isSaveEnabled = false

        val choices: List<Genre?> = listOf(null) + Genre.entries   // null = "Tous"
        val current = vm.filters.value.genre
        choices.forEach { genre ->
            val chip = layoutInflater.inflate(R.layout.item_genre_chip, group, false) as Chip
            chip.id = android.view.View.generateViewId()
            chip.text = getString(genre?.labelRes() ?: R.string.genre_all)
            chip.tag = genre                     // on retrouve le genre au clic
            chip.isSaveEnabled = false
            group.addView(chip)
            if (genre == current) group.check(chip.id)
        }

        // Écouteur ajouté APRÈS avoir coché la chip courante (pas d'appel parasite).
        group.setOnCheckedStateChangeListener { chipGroup, checkedIds ->
            val chip = checkedIds.firstOrNull()?.let { chipGroup.findViewById<Chip>(it) }
                ?: return@setOnCheckedStateChangeListener
            vm.setGenre(chip.tag as Genre?)
        }
    }

    // ---------------------------------------------------------------- menu ⋮

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_catalogue, menu)
        return true
    }

    /** Appelé après invalidateOptionsMenu() : on coche selon l'état du ViewModel. */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val filters = vm.filters.value
        menu.findItem(R.id.action_favorites)?.isChecked = filters.favoritesOnly
        val sortItem = when (filters.sort) {
            MovieSort.TITLE -> R.id.sort_title
            MovieSort.YEAR -> R.id.sort_year
            MovieSort.RATING -> R.id.sort_rating
        }
        menu.findItem(sortItem)?.isChecked = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        // Navigation vers les autres écrans
        R.id.action_lists -> { startActivity(Intent(this, WatchlistsActivity::class.java)); true }
        R.id.action_stats -> { startActivity(Intent(this, StatisticsActivity::class.java)); true }
        R.id.action_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
        // Filtres et tri
        R.id.action_favorites -> { vm.setFavoritesOnly(!item.isChecked); true }
        R.id.sort_title -> { vm.setSort(MovieSort.TITLE); true }
        R.id.sort_year -> { vm.setSort(MovieSort.YEAR); true }
        R.id.sort_rating -> { vm.setSort(MovieSort.RATING); true }
        else -> super.onOptionsItemSelected(item)
    }
}
