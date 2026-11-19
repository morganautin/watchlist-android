package fr.estiam.watchlist.ui.watchlist

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import fr.estiam.watchlist.R
import fr.estiam.watchlist.databinding.ActivityWatchlistsBinding
import fr.estiam.watchlist.ui.common.applySystemBarInsets
import fr.estiam.watchlist.ui.common.enableCinemaEdgeToEdge
import fr.estiam.watchlist.ui.common.showWatchlistDialog
import kotlinx.coroutines.launch

/**
 * ÉCRAN 4 — Mes listes : toutes les listes avec leur progression.
 * Toucher une liste ouvre son détail ; le bouton crée une nouvelle liste.
 */
class WatchlistsActivity : AppCompatActivity() {

    private lateinit var b: ActivityWatchlistsBinding
    private val vm: WatchlistsViewModel by viewModels { WatchlistsViewModel.Factory }

    private val adapter = WatchlistAdapter { summary ->
        startActivity(WatchlistDetailActivity.intent(this, summary.watchlist.id))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableCinemaEdgeToEdge()
        super.onCreate(savedInstanceState)
        b = ActivityWatchlistsBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applySystemBarInsets(b.toolbar, b.root)

        b.listRecycler.layoutManager = LinearLayoutManager(this)
        b.listRecycler.adapter = adapter

        // Création : boîte de dialogue avec contrôle de saisie, puis ViewModel.
        b.addListButton.setOnClickListener {
            showWatchlistDialog(R.string.new_list) { name, description -> vm.create(name, description) }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Liste réactive : elle se met à jour toute seule (création, statut changé…)
                launch {
                    vm.summaries.collect { summaries ->
                        if (summaries == null) return@collect   // encore en chargement
                        adapter.submitList(summaries)
                        b.emptyText.isVisible = summaries.isEmpty()
                    }
                }
                launch {
                    vm.events.collect { event ->
                        val message = when (event) {
                            is WatchlistsEvent.Created -> getString(R.string.list_created, event.name)
                            WatchlistsEvent.Failed -> getString(R.string.list_error)
                        }
                        Snackbar.make(b.root, message, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
