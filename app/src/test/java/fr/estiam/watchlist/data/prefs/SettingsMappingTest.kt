package fr.estiam.watchlist.data.prefs

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import fr.estiam.watchlist.model.MovieSort
import fr.estiam.watchlist.model.ThemeMode
import fr.estiam.watchlist.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests JUnit de la lecture des préférences DataStore (fichier → UserSettings).
 */
class SettingsMappingTest {

    @Test
    fun emptyFile_givesDefaultSettings() {
        // Premier lancement : rien n'est encore enregistré.
        assertEquals(UserSettings(), emptyPreferences().toUserSettings())
    }

    @Test
    fun storedValues_areRead() {
        val prefs = preferencesOf(
            DataStoreSettingsRepository.KEY_THEME to "DARK",
            DataStoreSettingsRepository.KEY_DEFAULT_SORT to "RATING",
            DataStoreSettingsRepository.KEY_HIDE_WATCHED to true
        )

        val settings = prefs.toUserSettings()

        assertEquals(ThemeMode.DARK, settings.theme)
        assertEquals(MovieSort.RATING, settings.defaultSort)
        assertEquals(true, settings.hideWatched)
    }

    @Test
    fun unknownValues_fallBackToDefaults() {
        // Ex. une valeur écrite par une ancienne version de l'app.
        val prefs = preferencesOf(
            DataStoreSettingsRepository.KEY_THEME to "PURPLE",
            DataStoreSettingsRepository.KEY_DEFAULT_SORT to "???"
        )

        val settings = prefs.toUserSettings()

        assertEquals(ThemeMode.SYSTEM, settings.theme)
        assertEquals(MovieSort.TITLE, settings.defaultSort)
    }
}
