package fr.estiam.watchlist.ui.common

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.estiam.watchlist.R
import fr.estiam.watchlist.databinding.DialogWatchlistBinding
import fr.estiam.watchlist.model.Watchlist

/**
 * Boîte de dialogue « Nouvelle liste » / « Modifier la liste » (formulaire avec contrôle de saisie).
 * Utilisée par 3 écrans : Mes listes, Détail d'une liste et Détail d'un film.
 *
 * @param initial liste existante à modifier, ou null pour une création.
 * @param onSave appelé avec le nom et la description, UNIQUEMENT si la saisie est valide.
 */
fun Context.showWatchlistDialog(
    @StringRes title: Int,
    initial: Watchlist? = null,
    onSave: (name: String, description: String) -> Unit
) {
    val b = DialogWatchlistBinding.inflate(LayoutInflater.from(this))
    b.nameInput.setText(initial?.name.orEmpty())
    b.descriptionInput.setText(initial?.description.orEmpty())
    b.nameInput.doAfterTextChanged { b.nameLayout.error = null }

    val dialog = MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setView(b.root)
        .setNegativeButton(R.string.cancel, null)
        // null ici : on gère le clic nous-mêmes plus bas, pour NE PAS fermer
        // la boîte si le nom est invalide (sinon Android la ferme automatiquement).
        .setPositiveButton(R.string.save, null)
        .create()

    dialog.setOnShowListener {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val name = b.nameInput.text?.toString()?.trim().orEmpty()
            val description = b.descriptionInput.text?.toString()?.trim().orEmpty()
            // Même règle que Watchlist.isValid() : nom non vide, 50 caractères maximum.
            if (!Watchlist(name = name).isValid()) {
                b.nameLayout.error = getString(R.string.error_list_name)
            } else {
                onSave(name, description)
                dialog.dismiss()
            }
        }
    }
    dialog.show()
}
