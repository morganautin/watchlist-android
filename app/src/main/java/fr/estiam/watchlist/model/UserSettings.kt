package fr.estiam.watchlist.model

/**
 * Les 3 préférences de l'application (enregistrées avec DataStore).
 * Les valeurs par défaut ci-dessous s'appliquent au premier lancement.
 */
data class UserSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,     // VISIBLE : thème clair / sombre
    val defaultSort: MovieSort = MovieSort.TITLE, // VISIBLE : ordre du catalogue
    val hideWatched: Boolean = false              // masque les films déjà vus du catalogue
)

/** SYSTEM = suit le réglage du téléphone. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }
