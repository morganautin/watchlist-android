package fr.estiam.watchlist

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.estiam.watchlist.ui.catalogue.CatalogueActivity
import fr.estiam.watchlist.ui.movie.MovieFormActivity
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TEST ESPRESSO 2/3 — Parcours complet d'un film (CRUD n° 1 + recherche).
 *
 * Scénario : ajouter un film avec le formulaire → le retrouver avec la
 * recherche du catalogue → ouvrir sa fiche → le supprimer → il disparaît.
 * Le film est supprimé à la fin : le test ne laisse aucune donnée derrière lui.
 */
@RunWith(AndroidJUnit4::class)
class MovieCrudFlowTest {

    private val context = ApplicationProvider.getApplicationContext<WatchListApplication>()

    @Test
    fun addSearchOpenAndDeleteMovie() {
        // Titre unique pour ne pas confondre avec un film existant
        val title = "Film Espresso ${System.currentTimeMillis()}"

        // ---- 1. Ajout avec le formulaire
        ActivityScenario.launch(MovieFormActivity::class.java)
        onView(withId(R.id.titleInput)).perform(replaceText(title))
        onView(withId(R.id.yearInput)).perform(replaceText("2020"))
        onView(withId(R.id.durationInput)).perform(replaceText("95"), closeSoftKeyboard())
        // Liste déroulante des genres : elle s'ouvre dans une fenêtre "popup"
        onView(withId(R.id.genreInput)).perform(scrollTo(), click())
        waitForView(withText(context.getString(R.string.genre_comedy)), isPlatformPopup()).perform(click())
        onView(withId(R.id.saveButton)).perform(scrollTo(), click())

        // ---- 2. Recherche dans le catalogue
        ActivityScenario.launch(CatalogueActivity::class.java).use {
            onView(withId(R.id.searchInput)).perform(replaceText(title), closeSoftKeyboard())

            // La ligne du film (titleText) — pas le champ de recherche qui contient le même texte
            val movieRow = allOf(withId(R.id.titleText), withText(title))
            waitForView(movieRow).perform(click())

            // ---- 3. Fiche du film puis suppression avec confirmation
            waitForView(allOf(withId(R.id.titleText), withText(title)))
            onView(withId(R.id.deleteButton)).perform(scrollTo(), click())
            waitForView(withText(R.string.delete), isDialog()).perform(click())

            // ---- 4. Retour au catalogue : le film n'y est plus
            waitUntilGone(movieRow)
        }
    }
}
