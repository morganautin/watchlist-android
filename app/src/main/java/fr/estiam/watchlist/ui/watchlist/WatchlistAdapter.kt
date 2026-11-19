package fr.estiam.watchlist.ui.watchlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.estiam.watchlist.R
import fr.estiam.watchlist.databinding.ItemWatchlistBinding
import fr.estiam.watchlist.model.WatchlistSummary

/**
 * Adapter de « Mes listes ».
 * ListAdapter (et non PagingDataAdapter) : le nombre de listes reste petit,
 * pas besoin de pagination ici.
 */
class WatchlistAdapter(
    private val onClick: (WatchlistSummary) -> Unit
) : ListAdapter<WatchlistSummary, WatchlistAdapter.ViewHolder>(DIFF) {

    class ViewHolder(val binding: ItemWatchlistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemWatchlistBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val summary = getItem(position)
        with(holder.binding) {
            nameText.text = summary.watchlist.name
            descriptionText.text = summary.watchlist.description
            descriptionText.isVisible = summary.watchlist.description.isNotBlank()
            // Barre de progression : % de films vus dans la liste
            progressBar.progress = summary.progressPercent
            progressText.text = root.context.getString(
                R.string.list_progress, summary.watchedCount, summary.totalCount, summary.progressPercent
            )
            root.setOnClickListener { onClick(summary) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<WatchlistSummary>() {
            override fun areItemsTheSame(oldItem: WatchlistSummary, newItem: WatchlistSummary) =
                oldItem.watchlist.id == newItem.watchlist.id
            override fun areContentsTheSame(oldItem: WatchlistSummary, newItem: WatchlistSummary) =
                oldItem == newItem
        }
    }
}
