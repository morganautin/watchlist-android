package fr.estiam.watchlist.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.estiam.watchlist.model.Movie
import fr.estiam.watchlist.model.Watchlist
import fr.estiam.watchlist.model.WatchlistItem

/**
 * La base de données Room de l'application (fichier "watchlist.db" sur le téléphone).
 *
 * On y déclare :
 *  - les tables (entities) : les 3 entités métier + la table technique AppMeta ;
 *  - la version du schéma (à augmenter si on modifie une table plus tard) ;
 *  - les DAO accessibles.
 */
@Database(
    entities = [Movie::class, Watchlist::class, WatchlistItem::class, AppMeta::class],
    version = 1,
    exportSchema = false
)
abstract class WatchListDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        // Une seule instance pour toute l'application : ouvrir une base coûte cher.
        @Volatile private var instance: WatchListDatabase? = null

        fun getInstance(context: Context): WatchListDatabase =
            // Double vérification : la base n'est créée qu'une fois,
            // même si deux threads la demandent en même temps.
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,   // contexte de l'app : pas de fuite d'Activity
                    WatchListDatabase::class.java,
                    "watchlist.db"
                ).build().also { instance = it }
            }
    }
}
