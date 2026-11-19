package fr.estiam.watchlist.ui.watchlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.estiam.watchlist.R
import fr.estiam.watchlist.databinding.ActivityWatchlistDetailBinding
import fr.estiam.watchlist.ui.common.applySystemBarInsets
import fr.estiam.watchlist.ui.common.enableCinemaEdgeToEdge
import fr.estiam.watchlist.ui.common.showWatchlistDialog
import fr.estiam.watchlist.ui.movie.MovieDetailActivity
import kotlinx.coroutines.launch

/**
 * ÉCRAN 5 — Détail d'une liste.
 * Changer le statut d'un film met à jour la progression en direct.
 */
class WatchlistDetailActivity : AppCompatActivity() {

    private lateinit var b: ActivityWatchlistDetailBinding
    private val vm: WatchlistDetailViewModel by viewModels { WatchlistDetailViewModel.Factory }
    private var deleteRequested = false

    private val adapter = WatchlistEntryAdapter(
        onOpen = { entry -> startActivity(MovieDetailActivity.intent(this, entry.movie.id)) },
        onStatus = { entry, status -> vm.setStatus(entry.movie.id, status) },
        onRemove = { entry -> vm.removeMovie(entry.movie.id) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        enableCinemaEdgeToEdge()
        super.onCreate(savedInstanceState)
        b = ActivityWatchlistDetailBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applySystemBarInsets(b.toolbar, b.root)

        b.entryRecycler.layoutManager = LinearLayoutManager(this)
        b.entryRecycler.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { state ->
                    when (state) {
                        WatchlistDetailState.Loading -> b.header.isVisible = false
                        is WatchlistDetailState.Loaded -> show(state)
                        WatchlistDetailState.NotFound -> {
                            val message = if (deleteRequested) R.string.list_deleted else R.string.list_not_found
                            Toast.makeText(this@WatchlistDetailActivity, message, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun show(state: WatchlistDetailState.Loaded) {
        supportActionBar?.title = state.watchlist.name
        b.header.isVisible = true
        b.descriptionText.text = state.watchlist.description
        b.descriptionText.isVisible = state.watchlist.description.isNotBlank()
        b.progressBar.progress = state.progressPercent
        b.progressText.text = getString(
            R.string.list_progress, state.watchedCount, state.entries.size, state.progressPercent
        )
        adapter.submitList(state.entries)
        b.emptyText.isVisible = state.entries.isEmpty()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_watchlist_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val loaded = vm.state.value as? WatchlistDetailState.Loaded
        return when (item.itemId) {
            R.id.action_rename -> {
                if (loaded != null) {
                    showWatchlistDialog(R.string.rename_list, loaded.watchlist) { name, description ->
                        vm.update(name, description)
                    }
                }
                true
            }
            R.id.action_delete -> {
                if (loaded != null) confirmDelete(loaded.watchlist.name)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmDelete(name: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_list)
            .setMessage(getString(R.string.delete_list_message, name))
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
        fun intent(context: Context, watchlistId: Long): Intent =
            Intent(context, WatchlistDetailActivity::class.java)
                .putExtra(WatchlistDetailViewModel.EXTRA_WATCHLIST_ID, watchlistId)
    }
}
