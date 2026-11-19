package fr.estiam.watchlist.ui.catalogue

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fr.estiam.watchlist.R
import fr.estiam.watchlist.databinding.ItemMovieBinding
import fr.estiam.watchlist.model.Movie
import fr.estiam.watchlist.ui.common.color
import fr.estiam.watchlist.ui.common.formatDuration
import fr.estiam.watchlist.ui.common.labelRes
import fr.estiam.watchlist.ui.common.ratingStars

/**
 * Adapter de la RecyclerView du catalogue.
 * PagingDataAdapter = adapter spécial Paging 3 : il reçoit les films page par page.
 *
 * @param onClick action quand on touche un film (ouvrir son détail).
 */
class MovieAdapter(
    private val onClick: (Movie) -> Unit
) : PagingDataAdapter<Movie, MovieAdapter.MovieViewHolder>(DIFF) {

    /** Garde les vues d'une ligne pour ne pas les rechercher à chaque affichage. */
    class MovieViewHolder(val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        // getItem peut renvoyer null si la page n'est pas encore chargée.
        val movie = getItem(position) ?: return
        val context = holder.itemView.context
        with(holder.binding) {
            // "Affiche" : couleur du genre + première lettre du titre
            poster.setBackgroundColor(movie.genre.color())
            poster.text = movie.title.firstOrNull()?.uppercase() ?: "?"

            titleText.text = movie.title
            metaText.text = context.getString(
                R.string.movie_meta, movie.year, context.getString(movie.genre.labelRes()),
                formatDuration(movie.durationMinutes)
            )
            ratingText.text = if (movie.rating == null) context.getString(R.string.not_rated)
            else ratingStars(movie.rating)
            favoriteIcon.isVisible = movie.isFavorite

            root.setOnClickListener { onClick(movie) }
        }
    }

    companion object {
        /**
         * DiffUtil compare l'ancienne et la nouvelle liste pour n'animer
         * que les lignes réellement modifiées (au lieu de tout redessiner).
         */
        private val DIFF = object : DiffUtil.ItemCallback<Movie>() {
            override fun areItemsTheSame(oldItem: Movie, newItem: Movie) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Movie, newItem: Movie) = oldItem == newItem
        }
    }
}
