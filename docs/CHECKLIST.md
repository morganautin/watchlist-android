# Checklist du projet final — à cocher au fur et à mesure

Rendu et présentation : **jeudi 26 novembre 2026, 16h**.
Légende : ✅ fait · 🔄 en cours · ⬜ à faire

## Interface et navigation (20 %)
- 🔄 Au moins 5 écrans fonctionnels — 3/7 faits (catalogue, détail film, formulaire film) ; restent mes listes, détail liste, statistiques, paramètres
- 🔄 Navigation entre les écrans (catalogue ↔ détail ↔ formulaire ✅)
- ✅ `RecyclerView` pour les collections (catalogue)
- 🔄 Formulaires avec contrôles de saisie (film ✅, liste à faire)
- ✅ Design cohérent (thème bleu nuit / or, clair + sombre)
- 🔄 États vides et gestion des erreurs (catalogue ✅ : chargement, vide, erreur + Réessayer)
- 🔄 Bon comportement à la rotation (ViewModels ✅, à vérifier sur émulateur)

## Room et architecture (25 %)
- ✅ 3 entités reliées : Movie, Watchlist, WatchlistItem (étape 1)
- 🔄 2 CRUD complets : films ✅, listes à faire
- ✅ DAO et Repositories : `MovieDao`, `WatchlistDao`, implémentations Room (étape 2)
- 🔄 ViewModels pour l'état des écrans (3 écrans ✅)
- ✅ Observation réactive avec Flow (détail du film mis à jour automatiquement)
- ✅ Paging 3 sur le catalogue
- ✅ Recherche, filtre par genre, favoris et tri
- 🔄 Données conservées après relance (Room ✅, à démontrer sur émulateur)

## Import JSON (dans les 15 %)
- ✅ Fichier `assets/movies.json` : 30 films valides + 3 entrées piégées
- ✅ Validation avant insertion (`MovieJsonParser` + `Movie.validate()`)
- ✅ Import une seule fois (marqueur dans la table `app_meta`, transaction)
- ✅ Ni doublons, ni écrasement (`OnConflictStrategy.IGNORE`)

## DataStore (dans les 15 %)
- ⬜ 3 préférences persistantes (thème, tri par défaut, masquer les vus)
- ⬜ Au moins 2 visibles (thème, tri)
- ⬜ Mise à jour sans rafraîchissement manuel
- ⬜ Conservées après relance

## Fonctionnalités métier
- 🔄 Statistiques par genre et par statut (requêtes ✅, écran à faire)
- 🔄 Progression de chaque liste (requête ✅, écran à faire)
- ⬜ Données cohérentes après modification (CASCADE ✅)

## Débogage et tests (20 %)
- ⬜ Utiliser Logcat, points d'arrêt et inspection des variables (garder des captures !)
- ⬜ Documenter 2 bugs (cause + solution) — 1 par membre
- ✅ Au moins 8 tests JUnit (11 écrits : validation, import JSON, formulaire)
- ⬜ Au moins 3 tests Espresso
- ⬜ Tous les tests lançables depuis le projet rendu

## Équipe et qualité (10 %)
- ⚠️ Équipe de 3 à 4 demandée — projet réalisé seul : à faire valider par le prof
- ⬜ Chaque membre contribue (commits depuis son propre compte)
- ✅ Dépôt GitHub partagé
- 🔄 Une branche par fonctionnalité + Pull Requests
- ⬜ Messages de commit clairs
- ⬜ README complet : projet, architecture, installation, fonctionnalités, tests, contributions

## Livrables et présentation (10 %)
- ✅ Schéma d'architecture (`docs/ARCHITECTURE.md`)
- ⬜ Application qui fonctionne pour la démo en direct
- ⬜ Présentation de 12 à 15 min : vue d'ensemble, démo, architecture et relations Room, Repository/Flow/Paging 3/DataStore, débogage et tests, contributions de chacun
- ⬜ Chaque membre présente et sait expliquer sa partie
