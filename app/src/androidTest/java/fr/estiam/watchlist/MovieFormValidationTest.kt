package fr.estiam.watchlist

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.estiam.watchlist.ui.movie.MovieFormActivity
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TEST ESPRESSO 1/3 — Contrôles de saisie du formulaire de film.
 *
 * Scénario : l'utilisateur ouvre « Ajouter un film » et touche « Enregistrer »
 * sans rien remplir → les erreurs s'affichent sous les champs et le formulaire
 * reste ouvert (aucun film invalide n'est enregistré).
 */
@RunWith(AndroidJUnit4::class)
class MovieFormValidationTest {

    @Test
    fun emptyForm_showsErrors_andStaysOpen() {
        // 1. Ouvrir le formulaire vide (mode ajout)
        ActivityScenario.launch(MovieFormActivity::class.java).use {

            // 2. Toucher « Enregistrer » sans rien saisir
            onView(withId(R.id.saveButton)).perform(scrollTo(), click())

            // 3. Une erreur sous chaque champ obligatoire
            waitUntil {
                onView(withId(R.id.titleLayout)).check(matches(hasInputError(R.string.error_empty_title)))
                onView(withId(R.id.yearLayout)).check(matches(hasInputError(R.string.error_year)))
                onView(withId(R.id.durationLayout)).check(matches(hasInputError(R.string.error_duration)))
                onView(withId(R.id.genreLayout)).check(matches(hasInputError(R.string.error_genre)))
            }

            // 4. Le formulaire est toujours affiché (pas d'enregistrement, pas de fermeture)
            onView(withId(R.id.titleInput)).perform(scrollTo()).check(matches(isDisplayed()))
        }
    }
}
