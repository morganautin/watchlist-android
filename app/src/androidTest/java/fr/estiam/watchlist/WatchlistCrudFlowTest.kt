package fr.estiam.watchlist

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.estiam.watchlist.ui.watchlist.WatchlistsActivity
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TEST ESPRESSO 3/3 — Parcours complet d'une liste (CRUD n° 2).
 *
 * Scénario : « Nouvelle liste » sans nom → erreur ; avec un nom → la liste
 * apparaît → on l'ouvre (elle est vide) → on la supprime → elle disparaît.
 */
@RunWith(AndroidJUnit4::class)
class WatchlistCrudFlowTest {

    @Test
    fun createOpenAndDeleteWatchlist() {
        val name = "Liste Espresso ${System.currentTimeMillis() % 1_000_000}"

        ActivityScenario.launch(WatchlistsActivity::class.java).use {
            // ---- 1. Contrôle de saisie : nom vide refusé, la boîte reste ouverte
            onView(withId(R.id.addListButton)).perform(click())
            waitForView(withText(R.string.save), isDialog()).perform(click())
            waitUntil {
                onView(withId(R.id.nameLayout)).inRoot(isDialog())
                    .check(matches(hasInputError(R.string.error_list_name)))
            }

            // ---- 2. Création avec un nom valide
            onView(withId(R.id.nameInput)).inRoot(isDialog()).perform(replaceText(name), closeSoftKeyboard())
            onView(withText(R.string.save)).inRoot(isDialog()).perform(click())

            val listRow = allOf(withId(R.id.nameText), withText(name))
            waitForView(listRow).perform(click())

            // ---- 3. Détail de la liste : message « liste vide »
            waitForView(withText(R.string.list_detail_empty))

            // ---- 4. Suppression via le menu ⋮, avec confirmation
            openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
            waitForView(withText(R.string.delete_list)).perform(click())
            waitForView(withText(R.string.delete), isDialog()).perform(click())

            // ---- 5. Retour à « Mes listes » : la liste n'existe plus
            waitUntilGone(listRow)
        }
    }
}
