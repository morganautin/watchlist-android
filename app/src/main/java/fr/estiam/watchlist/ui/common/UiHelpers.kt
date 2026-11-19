package fr.estiam.watchlist.ui.common

import android.graphics.Color
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import fr.estiam.watchlist.R
import fr.estiam.watchlist.model.Genre
import fr.estiam.watchlist.model.MovieError

/*
 * Petites fonctions partagées par tous les écrans :
 * libellés traduits, couleurs par genre, formatage, gestion des barres système.
 */

/** Libellé affiché d'un genre (texte dans strings.xml, jamais en dur dans le code). */
@StringRes
fun Genre.labelRes(): Int = when (this) {
    Genre.ACTION -> R.string.genre_action
    Genre.ANIMATION -> R.string.genre_animation
    Genre.COMEDY -> R.string.genre_comedy
    Genre.DOCUMENTARY -> R.string.genre_documentary
    Genre.DRAMA -> R.string.genre_drama
    Genre.FANTASY -> R.string.genre_fantasy
    Genre.HORROR -> R.string.genre_horror
    Genre.ROMANCE -> R.string.genre_romance
    Genre.SCIENCE_FICTION -> R.string.genre_science_fiction
    Genre.THRILLER -> R.string.genre_thriller
}

/** Couleur de "l'affiche" d'un film : une teinte par genre, pour repérer les films d'un coup d'œil. */
@ColorInt
fun Genre.color(): Int = when (this) {
    Genre.ACTION -> 0xFFC0392B.toInt()          // rouge
    Genre.ANIMATION -> 0xFF8E44AD.toInt()       // violet
    Genre.COMEDY -> 0xFFE0A526.toInt()          // or
    Genre.DOCUMENTARY -> 0xFF16A085.toInt()     // vert d'eau
    Genre.DRAMA -> 0xFF34495E.toInt()           // ardoise
    Genre.FANTASY -> 0xFF2E86C1.toInt()         // bleu
    Genre.HORROR -> 0xFF1B1B1B.toInt()          // noir
    Genre.ROMANCE -> 0xFFD35486.toInt()         // rose
    Genre.SCIENCE_FICTION -> 0xFF1C2340.toInt() // bleu nuit
    Genre.THRILLER -> 0xFF6D4C41.toInt()        // brun
}

/** Message d'erreur affiché sous un champ du formulaire. */
@StringRes
fun MovieError.messageRes(): Int = when (this) {
    MovieError.EMPTY_TITLE -> R.string.error_empty_title
    MovieError.TITLE_TOO_LONG -> R.string.error_title_too_long
    MovieError.INVALID_YEAR -> R.string.error_year
    MovieError.INVALID_DURATION -> R.string.error_duration
    MovieError.INVALID_RATING -> R.string.error_rating
    MovieError.MISSING_ID -> R.string.error_generic
}

/** 148 → "2 h 28" ; 45 → "45 min". */
fun formatDuration(minutes: Int): String =
    if (minutes < 60) "$minutes min" else "${minutes / 60} h ${"%02d".format(minutes % 60)}"

/** 3 → "★★★☆☆" ; null → "" (film non noté). */
fun ratingStars(rating: Int?): String =
    if (rating == null) "" else "★".repeat(rating) + "☆".repeat(5 - rating)

/**
 * Affichage "bord à bord" (obligatoire à partir d'Android 15) :
 * l'app dessine derrière la barre d'état et la barre de navigation.
 * À appeler AVANT setContentView. Icônes de la barre d'état en blanc
 * (car notre barre du haut est foncée).
 */
fun ComponentActivity.enableCinemaEdgeToEdge() {
    enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
}

/**
 * Évite que le contenu passe SOUS les barres système ou le clavier :
 *  - la barre du haut reçoit un espace en haut (hauteur de la barre d'état) ;
 *  - l'écran entier reçoit un espace en bas (barre de navigation ou clavier).
 */
fun applySystemBarInsets(toolbar: View, root: View) {
    ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.updatePadding(top = bars.top)
        insets
    }
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
        )
        view.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
        insets
    }
}
