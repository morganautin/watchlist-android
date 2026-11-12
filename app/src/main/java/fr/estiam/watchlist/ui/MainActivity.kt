package fr.estiam.watchlist.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.estiam.watchlist.databinding.ActivityMainBinding

/**
 * Écran provisoire : vérifie que le projet compile et se lance.
 * Il deviendra le catalogue des films à l'étape 3.
 */
class MainActivity : AppCompatActivity() {

    // Accès aux vues du fichier activity_main.xml (généré par ViewBinding)
    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. On "gonfle" le layout XML et on l'affiche
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        // 2. La barre du haut sert de barre d'action (titre + menus plus tard)
        setSupportActionBar(b.toolbar)
    }
}
