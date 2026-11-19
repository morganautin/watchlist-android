# Checklist du projet final — à cocher au fur et à mesure

Rendu et présentation : **jeudi 26 novembre 2026, 16h**.
Légende : ✅ fait · 🔄 en cours · ⬜ à faire

## Interface et navigation (20 %)
- ✅ Au moins 5 écrans fonctionnels — 7 écrans (catalogue, détail film, formulaire film, mes listes, détail liste, statistiques, paramètres)
- ✅ Navigation entre les écrans (icônes de la barre du haut + flèche retour)
- ✅ `RecyclerView` pour les collections (catalogue, listes, films d'une liste)
- ✅ Formulaires avec contrôles de saisie (film + liste)
- ✅ Design cohérent (thème bleu nuit / or, clair + sombre)
- ✅ États vides et gestion des erreurs (chargement, vide, erreur + Réessayer, messages)
- 🔄 Bon comportement à la rotation (ViewModels ✅, à vérifier sur émulateur)

## Room et architecture (25 %)
- ✅ 3 entités reliées : Movie, Watchlist, WatchlistItem (étape 1)
- ✅ 2 CRUD complets : films + listes
- ✅ DAO et Repositories : `MovieDao`, `WatchlistDao`, implémentations Room (étape 2)
- ✅ ViewModels pour l'état des écrans (un par écran)
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
- ✅ 3 préférences persistantes (thème, tri par défaut, masquer les vus)
- ✅ Au moins 2 visibles (thème de toute l'app, ordre du catalogue ; + films vus masqués)
- ✅ Mise à jour sans rafraîchissement manuel (Flow DataStore)
- 🔄 Conservées après relance (DataStore ✅, à démontrer sur émulateur)

## Fonctionnalités métier
- ✅ Statistiques par genre et par statut (écran Statistiques)
- ✅ Progression de chaque liste (Mes listes + détail d'une liste)
- ✅ Données cohérentes après modification (CASCADE + mise à jour automatique)

## Débogage et tests (20 %)
- 🔄 Utiliser Logcat, points d'arrêt et inspection des variables (procédure dans `docs/DEBUGGING.md`, captures à ajouter dans `docs/debug/`)
- 🔄 Documenter 2 bugs (cause + solution) — rédigés dans `docs/DEBUGGING.md`, captures à ajouter
- ✅ Au moins 8 tests JUnit (14 écrits : validation, import JSON, formulaire, préférences)
- ✅ Au moins 3 tests Espresso (formulaire, parcours film, parcours liste)
- 🔄 Tous les tests lançables depuis le projet rendu (à vérifier sur émulateur)

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
