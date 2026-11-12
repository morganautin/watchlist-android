package fr.estiam.watchlist

import android.app.Application

/**
 * Point d'entrée global de l'application, créé une seule fois au lancement.
 * Étape 2 : on y créera la base Room, les Repositories et le DataStore,
 * pour que tous les écrans partagent les mêmes instances.
 */
class WatchListApplication : Application()
