package fr.estiam.watchlist.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Petite table technique clé / valeur (pas une entité métier).
 * Sert à mémoriser que l'import JSON initial a déjà été fait :
 * clé "seed_movies_done" présente = on ne réimporte plus jamais.
 */
@Entity(tableName = "app_meta")
data class AppMeta(
    @PrimaryKey val key: String,
    val value: String
)
