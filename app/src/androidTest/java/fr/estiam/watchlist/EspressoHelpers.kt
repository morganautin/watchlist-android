package fr.estiam.watchlist

import android.os.SystemClock
import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher

/*
 * Outils communs aux tests Espresso.
 *
 * Pourquoi « attendre » ? Room, Paging et DataStore travaillent en arrière-plan :
 * après un clic, l'écran se met à jour quelques millisecondes plus tard.
 * Ces fonctions réessaient une vérification jusqu'à ce qu'elle réussisse
 * (ou échouent après un délai), au lieu d'échouer immédiatement.
 */

private const val DEFAULT_TIMEOUT_MS = 5_000L

/** Réessaie [assertion] toutes les 100 ms jusqu'à ce qu'elle passe, ou échoue après [timeoutMs]. */
fun waitUntil(timeoutMs: Long = DEFAULT_TIMEOUT_MS, assertion: () -> Unit) {
    val deadline = SystemClock.uptimeMillis() + timeoutMs
    var lastError: Throwable? = null
    while (SystemClock.uptimeMillis() < deadline) {
        try {
            assertion()
            return            // la vérification a réussi
        } catch (e: Throwable) {
            lastError = e     // pas encore : on attend un peu et on recommence
            SystemClock.sleep(100)
        }
    }
    throw AssertionError("Condition non remplie après $timeoutMs ms", lastError)
}

/**
 * Attend qu'une vue soit affichée, puis la renvoie (pour cliquer dessus, par exemple).
 * @param root fenêtre où chercher : null = écran principal, isDialog() = boîte de dialogue…
 */
fun waitForView(viewMatcher: Matcher<View>, root: Matcher<Root>? = null): ViewInteraction {
    lateinit var interaction: ViewInteraction
    waitUntil {
        interaction = onView(viewMatcher).let { if (root != null) it.inRoot(root) else it }
        interaction.check(matches(isDisplayed()))
    }
    return interaction
}

/** Attend qu'une vue ait disparu de l'écran (ex. un film supprimé de la liste). */
fun waitUntilGone(viewMatcher: Matcher<View>) = waitUntil { onView(viewMatcher).check(doesNotExist()) }

/**
 * Vérifie le message d'erreur affiché par un TextInputLayout (sous un champ de formulaire).
 * Exemple : onView(withId(R.id.titleLayout)).check(matches(hasInputError(R.string.error_empty_title)))
 */
fun hasInputError(@StringRes expected: Int): Matcher<View> =
    object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("un champ affichant l'erreur n° $expected")
        }

        override fun matchesSafely(item: TextInputLayout): Boolean =
            item.error?.toString() == item.context.getString(expected)
    }
