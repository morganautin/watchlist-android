package fr.estiam.watchlist.ui.stats

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import fr.estiam.watchlist.R
import fr.estiam.watchlist.databinding.ActivityStatisticsBinding
import fr.estiam.watchlist.databinding.ItemStatBarBinding
import fr.estiam.watchlist.model.CollectionStats
import fr.estiam.watchlist.model.WatchStatus
import fr.estiam.watchlist.ui.common.applySystemBarInsets
import fr.estiam.watchlist.ui.common.color
import fr.estiam.watchlist.ui.common.enableCinemaEdgeToEdge
import fr.estiam.watchlist.ui.common.labelRes
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * ÉCRAN 6 — Statistiques de la collection (fonctionnalité métier n° 1).
 * Noter un film, en ajouter un ou changer un statut met ces chiffres à jour.
 */
class StatisticsActivity : AppCompatActivity() {

    private lateinit var b: ActivityStatisticsBinding
    private val vm: StatisticsViewModel by viewModels { StatisticsViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableCinemaEdgeToEdge()
        super.onCreate(savedInstanceState)
        b = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applySystemBarInsets(b.toolbar, b.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.stats.collect { stats -> if (stats != null) show(stats) }
            }
        }
    }

    private fun show(stats: CollectionStats) {
        b.emptyText.isVisible = stats.totalMovies == 0
        b.content.isVisible = stats.totalMovies > 0

        // 1. Chiffres clés
        b.totalText.text = stats.totalMovies.toString()
        b.favoritesText.text = stats.favoriteCount.toString()
        b.averageText.text = stats.averageRating?.let { "★ " + formatAverage(it) }
            ?: getString(R.string.stat_none)

        // 2. Par genre : la plus longue barre = le genre le plus représenté
        b.genreContainer.removeAllViews()
        val maxGenre = stats.byGenre.maxOfOrNull { it.count } ?: 1
        stats.byGenre.forEach { genreStat ->
            val value = genreStat.averageRating
                ?.let { getString(R.string.genre_stat_value, genreStat.count, formatAverage(it)) }
                ?: getString(R.string.genre_stat_value_no_rating, genreStat.count)
            addBar(b.genreContainer, getString(genreStat.genre.labelRes()), value,
                genreStat.count, maxGenre, genreStat.genre.color())
        }

        // 3. Par statut : on affiche les 3 statuts, même ceux à 0
        b.statusContainer.removeAllViews()
        val counts = stats.byStatus.associate { it.status to it.count }
        val maxStatus = counts.values.maxOrNull()?.coerceAtLeast(1) ?: 1
        WatchStatus.entries.forEach { status ->
            val count = counts[status] ?: 0
            addBar(b.statusContainer, getString(status.labelRes()), count.toString(),
                count, maxStatus, getColor(R.color.cinema_gold))
        }
    }

    /** Ajoute une ligne « libellé — valeur » avec une barre proportionnelle. */
    private fun addBar(
        container: LinearLayout, label: String, value: String,
        count: Int, max: Int, @ColorInt color: Int
    ) {
        val row = ItemStatBarBinding.inflate(layoutInflater, container, false)
        row.labelText.text = label
        row.valueText.text = value
        row.bar.max = max
        row.bar.progress = count
        row.bar.setIndicatorColor(color)
        container.addView(row.root)
    }

    /** 4.333 → "4,3" (format français). */
    private fun formatAverage(value: Double): String = String.format(Locale.FRANCE, "%.1f", value)

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
