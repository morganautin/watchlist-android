# Checklist du projet final — à cocher au fur et à mesure

Rendu et présentation : **jeudi 26 novembre 2026, 16h**.
Légende : ✅ fait · 🔄 en cours · ⬜ à faire

## Interface et navigation (20 %)
- ⬜ Au moins 5 écrans fonctionnels (objectif 7 : catalogue, détail film, formulaire film, mes listes, détail liste, statistiques, paramètres)
- ⬜ Navigation entre les écrans
- ⬜ `RecyclerView` pour les collections
- ⬜ Formulaires avec contrôles de saisie (film, liste)
- ⬜ Design cohérent (thème bleu nuit / or)
- ⬜ États vides (« aucun film », « aucune liste ») et gestion des erreurs
- ⬜ Bon comportement à la rotation de l'écran

## Room et architecture (25 %)
- ✅ 3 entités reliées : Movie, Watchlist, WatchlistItem (étape 1)
- ⬜ 2 CRUD complets : films + listes
- ⬜ DAO et Repositories (contrats ✅, implémentation Room ⬜)
- ⬜ ViewModels pour l'état des écrans
- ⬜ Observation réactive avec Flow
- ⬜ Paging 3 sur le catalogue
- ⬜ Recherche, filtre et tri
- ⬜ Données conservées après relance

## Import JSON (dans les 15 %)
- ⬜ Fichier `assets/movies.json` avec au moins 20 films valides
- ⬜ Validation avant insertion (`Movie.validate()` ✅)
- ⬜ Import une seule fois
- ⬜ Ni doublons, ni écrasement des modifications de l'utilisateur

## DataStore (dans les 15 %)
- ⬜ 3 préférences persistantes (thème, tri par défaut, masquer les vus)
- ⬜ Au moins 2 visibles (thème, tri)
- ⬜ Mise à jour sans rafraîchissement manuel
- ⬜ Conservées après relance

## Fonctionnalités métier
- ⬜ Statistiques par genre et par statut
- ⬜ Progression de chaque liste (% de films vus)
- ⬜ Données cohérentes après modification (CASCADE ✅)

## Débogage et tests (20 %)
- ⬜ Utiliser Logcat, points d'arrêt et inspection des variables (garder des captures !)
- ⬜ Documenter 2 bugs (cause + solution) — 1 par membre
- 🔄 Au moins 8 tests JUnit (6 écrits à l'étape 1)
- ⬜ Au moins 3 tests Espresso
- ⬜ Tous les tests lançables depuis le projet rendu

## Équipe et qualité (10 %)
- ⚠️ Équipe de 3 à 4 demandée — actuellement 2 : trouver un membre ou faire valider par le prof
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
