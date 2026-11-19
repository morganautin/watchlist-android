# WatchList — Gestionnaire de films et de watchlists

Projet final **ESTIAM E5 — Mobile Apps Development** (Android natif, Kotlin).

> README en cours de rédaction : il sera complété au fil du projet
> (architecture, fonctionnalités, tests, contributions).

## Équipe
- morganautin
- _(à compléter)_

## Documentation
- [Architecture et base de données](docs/ARCHITECTURE.md) — schémas des couches et des relations Room
- [Checklist des exigences](docs/CHECKLIST.md) — avancement du projet

## Structure du code
- `model/` — entités Room et objets métier (contrats communs de l'équipe)
- `data/` — interfaces des Repositories, puis leurs implémentations Room / DataStore
- `ui/` — écrans (Activities), adapters et ViewModels

## Installation
1. Cloner le dépôt et ouvrir le dossier racine dans Android Studio.
2. JDK 17 ou 21 (Gradle JDK dans *Settings → Build Tools → Gradle*).
3. Lancer **Sync Project with Gradle Files**, puis **Run** sur un émulateur (API 26+).

## Organisation Git
- `main` : version stable, modifiée uniquement par Pull Request.
- Une branche par fonctionnalité : `feature/<nom>` (ex. `feature/room-database`).
- Commits courts et explicites en français.
