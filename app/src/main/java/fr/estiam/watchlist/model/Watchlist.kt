package fr.estiam.watchlist.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ENTITÉ 2/3 — Une liste de films créée par l'utilisateur
 * (ex. « Films de Noël », « À voir avec Léa »).
 */
@Entity(tableName = "watchlists")
data class Watchlist(
    // autoGenerate : Room attribue lui-même 1, 2, 3… à la création.
    // On passe 0 pour une nouvelle liste.
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Contrôle de saisie du formulaire « nouvelle liste ». */
    fun isValid(): Boolean = name.isNotBlank() && name.length <= MAX_NAME_LENGTH

    companion object { const val MAX_NAME_LENGTH = 50 }
}

/**
 * ENTITÉ 3/3 — Table de liaison entre une liste et un film.
 *
 * Relation « plusieurs à plusieurs » :
 *  - une liste contient plusieurs films ;
 *  - un film peut être dans plusieurs listes.
 * Chaque ligne porte aussi le statut de visionnage du film DANS cette liste.
 */
@Entity(
    tableName = "watchlist_items",
    // Clé primaire composée : un film ne peut être qu'une fois dans une même liste.
    primaryKeys = ["watchlistId", "movieId"],
    foreignKeys = [
        // Si on supprime la liste → ses lignes sont supprimées automatiquement (CASCADE).
        ForeignKey(
            entity = Watchlist::class, parentColumns = ["id"], childColumns = ["watchlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        // Si on supprime le film → il disparaît de toutes les listes (données cohérentes).
        ForeignKey(
            entity = Movie::class, parentColumns = ["id"], childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Index recommandé par Room sur la colonne de clé étrangère.
    indices = [Index("movieId")]
)
data class WatchlistItem(
    val watchlistId: Long,
    val movieId: String,
    val status: WatchStatus = WatchStatus.TO_WATCH,
    val addedAt: Long = System.currentTimeMillis()
)

/** Statut de visionnage d'un film dans une liste. */
enum class WatchStatus { TO_WATCH, WATCHING, WATCHED }

// ---------------------------------------------------------------------------
// Objets "résultat" : pas des tables, mais des réponses de requêtes Room
// qui combinent plusieurs tables (JOIN). @Embedded = « recopie les colonnes
// de cet objet ».
// ---------------------------------------------------------------------------

/** Une liste + sa progression (fonctionnalité métier n° 2). Écran « Mes listes ». */
data class WatchlistSummary(
    @Embedded val watchlist: Watchlist,
    val totalCount: Int,     // nombre de films dans la liste
    val watchedCount: Int    // dont films au statut WATCHED
) {
    /** Progression en pourcentage, 0 si la liste est vide (évite la division par zéro). */
    val progressPercent: Int
        get() = if (totalCount == 0) 0 else watchedCount * 100 / totalCount
}

/** Un film tel qu'il apparaît dans une liste, avec son statut. Écran « Détail d'une liste ». */
data class WatchlistEntry(
    @Embedded val movie: Movie,
    val status: WatchStatus,
    val addedAt: Long
)
