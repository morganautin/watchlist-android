package fr.estiam.watchlist.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import fr.estiam.watchlist.data.SettingsRepository
import fr.estiam.watchlist.model.MovieSort
import fr.estiam.watchlist.model.ThemeMode
import fr.estiam.watchlist.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Un seul DataStore par fichier dans toute l'application : on le déclare
 * ici, attaché au Context (fichier "user_settings.preferences_pb" sur le téléphone).
 */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

/**
 * Implémentation DataStore du contrat [SettingsRepository] : les 3 préférences
 * de l'app, conservées après fermeture et relance.
 *
 * @param dataStore passé en paramètre (et non créé ici) pour garder la classe simple et testable.
 */
class DataStoreSettingsRepository(private val dataStore: DataStore<Preferences>) : SettingsRepository {

    /**
     * Flux observé par les écrans : il émet une nouvelle valeur à CHAQUE changement
     * de préférence → l'app se met à jour sans rafraîchissement manuel.
     */
    override val settings: Flow<UserSettings> = dataStore.data
        // Fichier illisible : on repart des valeurs par défaut au lieu de planter.
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { prefs -> prefs.toUserSettings() }
        .distinctUntilChanged()

    // edit {} écrit sur le disque, hors du thread principal, de façon sûre (transaction).
    override suspend fun setTheme(theme: ThemeMode) {
        dataStore.edit { it[KEY_THEME] = theme.name }
    }

    override suspend fun setDefaultSort(sort: MovieSort) {
        dataStore.edit { it[KEY_DEFAULT_SORT] = sort.name }
    }

    override suspend fun setHideWatched(hide: Boolean) {
        dataStore.edit { it[KEY_HIDE_WATCHED] = hide }
    }

    companion object {
        // Les clés : leur nom est celui enregistré dans le fichier.
        val KEY_THEME = stringPreferencesKey("pref_theme")
        val KEY_DEFAULT_SORT = stringPreferencesKey("pref_default_sort")
        val KEY_HIDE_WATCHED = booleanPreferencesKey("pref_hide_watched")
    }
}

/**
 * Conversion fichier → objet UserSettings. Fonction "pure", testée avec JUnit.
 * Valeur absente ou inconnue (ex. ancienne version de l'app) → valeur par défaut.
 */
fun Preferences.toUserSettings(): UserSettings {
    val defaults = UserSettings()
    return UserSettings(
        theme = ThemeMode.entries.firstOrNull { it.name == this[DataStoreSettingsRepository.KEY_THEME] }
            ?: defaults.theme,
        defaultSort = MovieSort.entries.firstOrNull { it.name == this[DataStoreSettingsRepository.KEY_DEFAULT_SORT] }
            ?: defaults.defaultSort,
        hideWatched = this[DataStoreSettingsRepository.KEY_HIDE_WATCHED] ?: defaults.hideWatched
    )
}
