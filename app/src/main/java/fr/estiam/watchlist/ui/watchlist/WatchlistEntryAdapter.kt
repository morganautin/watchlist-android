package fr.estiam.watchlist.ui.watchlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.estiam.watchlist.R
import fr.estiam.watchlist.databinding.ItemWatchlistEntryBinding
import fr.estiam.watchlist.model.WatchStatus
import fr.estiam.watchlist.model.WatchlistEntry
import fr.estiam.watchlist.ui.common.color
import fr.estiam.watchlist.ui.common.formatDuration
import fr.estiam.watchlist.ui.common.labelRes

/**
 * Adapter des films d'une liste.
 * @param onOpen ouvrir la fiche du film
 * @param onStatus nouveau statut choisi
 * @param onRemove retirer le film de la liste
 */
class WatchlistEntryAdapter(
    private val onOpen: (WatchlistEntry) -> Unit,
    private val onStatus: (WatchlistEntry, WatchStatus) -> Unit,
    private val onRemove: (WatchlistEntry) -> Unit
) : ListAdapter<WatchlistEntry, WatchlistEntryAdapter.ViewHolder>(DIFF) {

    class ViewHolder(val binding: ItemWatchlistEntryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemWatchlistEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getItem(position)
        val movie = entry.movie
        with(holder.binding) {
            poster.setBackgroundColor(movie.genre.color())
            poster.text = movie.title.firstOrNull()?.uppercase() ?: "?"
            titleText.text = movie.title
            metaText.text = "${movie.year} · ${formatDuration(movie.durationMinutes)}"
            statusButton.setText(entry.status.labelRes())

            root.setOnClickListener { onOpen(entry) }
            statusButton.setOnClickListener { button -> showStatusMenu(button, entry) }
        }
    }

    /** Petit menu sous le bouton : les 3 statuts + « Retirer de la liste ». */
    private fun showStatusMenu(anchor: android.view.View, entry: WatchlistEntry) {
        val popup = PopupMenu(anchor.context, anchor)
        WatchStatus.entries.forEachIndexed { index, status ->
            // itemId = position de l'enum, pour retrouver le statut au clic
            popup.menu.add(0, index, index, status.labelRes())
        }
        popup.menu.add(1, REMOVE_ID, WatchStatus.entries.size, R.string.remove_from_list)
        popup.setOnMenuItemClickListener { item ->
            if (item.itemId == REMOVE_ID) onRemove(entry)
            else onStatus(entry, WatchStatus.entries[item.itemId])
            true
        }
        popup.show()
    }

    companion object {
        private const val REMOVE_ID = 99

        private val DIFF = object : DiffUtil.ItemCallback<WatchlistEntry>() {
            override fun areItemsTheSame(oldItem: WatchlistEntry, newItem: WatchlistEntry) =
                oldItem.movie.id == newItem.movie.id
            override fun areContentsTheSame(oldItem: WatchlistEntry, newItem: WatchlistEntry) =
                oldItem == newItem
        }
    }
}
