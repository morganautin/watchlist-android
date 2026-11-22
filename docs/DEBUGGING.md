# Débogage et tests

## 1. Lancer les tests

| Type | Où | Comment |
|---|---|---|
| **JUnit** (14 tests, sur l'ordinateur) | `app/src/test/` | Terminal : `.\gradlew.bat testDebugUnitTest` — ou clic droit sur le dossier `test` → *Run 'Tests in…'* |
| **Espresso** (3 tests, sur l'émulateur) | `app/src/androidTest/` | Émulateur lancé, puis clic droit sur le dossier `androidTest` → *Run 'Tests in…'* |

### Tests JUnit
| Fichier | Ce qui est vérifié |
|---|---|
| `ModelValidationTest` (6) | Règles de validation d'un film et d'une liste, calcul de la progression |
| `MovieJsonParserTest` (3) | Import JSON : 30 films valides, entrées invalides et doublons rejetés, valeurs par défaut |
| `MovieFormLogicTest` (2) | Saisie du formulaire → film (nombres mal tapés, conservation de l'id en modification) |
| `SettingsMappingTest` (3) | Lecture des préférences DataStore (fichier vide, valeurs enregistrées, valeurs inconnues) |

### Tests Espresso (interface)
| Fichier | Scénario |
|---|---|
| `MovieFormValidationTest` | Formulaire vide → une erreur sous chaque champ obligatoire, le formulaire reste ouvert |
| `MovieCrudFlowTest` | Ajouter un film → le retrouver avec la recherche → ouvrir sa fiche → le supprimer |
| `WatchlistCrudFlowTest` | Nom de liste vide refusé → créer une liste → l'ouvrir → la supprimer |

Les tests Espresso suppriment ce qu'ils créent : ils ne laissent aucune donnée dans l'application.

---

## 2. Bugs rencontrés et corrigés

### Bug n° 1 — « Ajouter à une liste » ignore les listes existantes

**Outils utilisés :** point d'arrêt (breakpoint) + inspection des variables + Logcat (filtre `MovieDetail`)

**Symptôme.** Des listes existent dans « Mes listes », mais sur la fiche d'un film, le bouton
« Ajouter à une liste » ouvre directement « Nouvelle liste » au lieu de proposer les listes à cocher.

**Analyse.**
1. Point d'arrêt sur la ligne `if (lists.isEmpty())` de `MovieDetailActivity.chooseLists()`.
2. Lancement en mode débogage (🐞), clic sur « Ajouter à une liste » : l'exécution s'arrête.
3. Panneau *Variables* : `lists` est vide (`size = 0`) alors que la base contient des listes.
4. Logcat confirme : `Ajouter à une liste : 0 liste(s) disponible(s)`.

**Cause.** Dans `MovieDetailViewModel`, les listes étaient exposées avec
`stateIn(..., SharingStarted.WhileSubscribed(5_000), emptyList())`.
Avec `WhileSubscribed`, la requête Room ne démarre **que si quelqu'un observe (collecte) le flux**.
Or l'écran ne fait que lire `.value` au moment du clic, sans observer : la requête n'était jamais lancée
et la valeur restait la valeur initiale `emptyList()`.

**Solution.** Démarrer l'observation dès la création du ViewModel :
`SharingStarted.Eagerly`. La valeur est alors toujours à jour au moment du clic,
et la boîte « Dans quelles listes ? » propose bien les listes existantes.

| Avant | Après |
|---|---|
| ![point d'arrêt : lists vide](debug/bug1-breakpoint.png) | ![corrigé : les listes sont proposées](debug/bug1-corrige.png) |

---

### Bug n° 2 — Plantage du catalogue après la suppression d'un film

**Outils utilisés :** test Espresso `MovieCrudFlowTest` + Logcat (pile d'appels de l'exception)

**Symptôme.** Le test Espresso `MovieCrudFlowTest` échouait au moment de confirmer la suppression
d'un film. En revenant de la fiche du film vers le catalogue, l'application plantait.

**Analyse.**
1. Le test échoue sur le clic « Supprimer » de la boîte de confirmation (`PerformException`).
2. Dans Logcat, la cause réelle apparaît sous `Caused by` :
   `IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter position … app:id/movieList … MovieAdapter`
3. Ce n'est donc pas le test qui est en cause : c'est la **RecyclerView du catalogue** (`movieList`)
   qui plante pendant son affichage, au moment où l'écran revient au premier plan.

**Cause.** Dans `CatalogueActivity`, les pages du catalogue étaient envoyées à l'adapter
**à l'intérieur de `repeatOnLifecycle(STARTED)`** :
- quand on ouvre la fiche du film, le catalogue passe en arrière-plan → la collecte est **arrêtée** ;
- on supprime le film → Room met à jour les données du catalogue pendant ce temps ;
- au retour, la collecte **redémarre** et renvoie à l'adapter la même `PagingData` encore en cours de mise à jour.

L'adapter et la RecyclerView ne sont plus d'accord sur le nombre de lignes → plantage.

**Solution.** Collecter les pages avec `lifecycleScope.launch { … submitData(it) }`, **en dehors**
de `repeatOnLifecycle` (méthode recommandée par la documentation Paging 3) :
l'adapter Paging gère lui-même le cycle de vie et reçoit les mises à jour en continu.
Les autres observations (état du chargement, filtres, messages) restent liées au cycle de vie.

**Vérification.** Après correction, les 3 tests Espresso passent.

| Avant | Après |
|---|---|
| ![test en échec et Logcat](debug/bug2-test-echec.png) | ![3 tests réussis](debug/bug2-tests-ok.png) |

---

## 3. Autres outils utilisés pendant le développement
- **Logcat, tag `WatchListSeed`** : vérifier l'import JSON (`30 films valides, 3 rejetés` au premier lancement uniquement).
- **Logcat, tag `CatalogueVM`** : erreurs éventuelles de l'import affichées avec leur pile d'appels.
- **App Inspection → Database Inspector** (Android Studio) : contenu des tables Room en direct.
