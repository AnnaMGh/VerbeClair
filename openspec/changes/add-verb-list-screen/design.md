## Context

The repository has three modules today: `:shared` (the template `App()`), `:androidApp`, and `:core:designsystem`
(theme, icons, flag helpers and every component Screen 1 needs). There is no build-logic, Room, Koin, DataStore or
navigation. Module build files are plain (see `core/designsystem/build.gradle.kts`): `com.android.kotlin.multiplatform.library`,
`iosArm64` + `iosSimulatorArm64`, `commonTest` run as Android device tests and on the iOS simulator. See proposal.md for
scope and the specs for behaviour (`verb-data`, `user-data`, `verb-list-screen`).

The Figma values below were read with `get_metadata` / `get_design_context` on the Screens frames L-01…L-09, PL-01,
TP-01 and TL-01 and on the list-controls sheet (10:519). They must be re-read per task before implementing.

## Goals / Non-Goals

**Goals:**
- One verb-data definition (entities + mapping) shared by the runtime and by the JVM task that builds the bundled
  database, so the bundled file has Room's exact schema identity.
- One normaliser shared by key building and query building, so search never drifts from the stored keys.
- A screen that is a single `LazyVerticalGrid` on all window sizes (1 or 2 columns), fed by `PagingData` with section
  separators, so grouping, sticky headers and paging behave the same everywhere.

**Non-Goals:**
- Network API client, sync, WorkManager / BGTask, flavors (A-5, A-9, SY-*).
- Convention plugins / `build-logic`. Five new modules are added with plain build files that follow
  `:core:designsystem`. Extracting convention plugins is a separate refactor.
- Navigation library and per-tab back stacks (NV-2). The shell holds the selected tab in state; only Verbs exists.

## Decisions

### D1. Modules

| Module | Plugins | Holds |
|---|---|---|
| `:core:domain` | KMP library (+ JVM target) | `Verb`, `VerbListItem`, `VerbGroup`, `Auxiliary`, `ListQuery` (search, filters, groupBy, sort, tab), `TestResult`; interfaces `VerbCatalog`, `FavouritesRepository`, `TestScoreRepository`, `ListPreferences`, `DataSyncTrigger`; `normalizeKey()` / `sortKey()` (pure Kotlin) |
| `:core:database` | KMP library (+ JVM target), KSP, Room | `VerbDatabase` (verbs, forms, translations, synonyms, `form_search` keys) and `UserDatabase` (favourites, test_attempts, attempt_lines), DAOs, `DatasetRecord` (kotlinx-serialization model of the dataset JSON), the shared dataset → entity mapper, `VERB_SCHEMA_VERSION`, `BUNDLE_BUILD` |
| `:core:data` | KMP library | `RoomVerbCatalog`, `RoomFavouritesRepository`, `RoomTestScoreRepository`, `DataStoreListPreferences`, `BundledVerbDatabaseInstaller` (+ `expect`/`actual` file access), `NoOpDataSyncTrigger`, `PlatformPaths` + `expect val platformModule`, Koin `coreDataModule` |
| `:core:presentation` | Compose MP library | `WindowLayout` (D8), `UiText`, `ObserveAsEvents` if needed |
| `:feature:verblist` | Compose MP library | `VerbListViewModel`, `VerbListState`/`Action`, `VerbListRoot`/`VerbListScreen`, `ListControlsSheet`, `ActiveChoicesRow`, UI models + label mapping, strings (en/fr/ro), previews, Koin `verbListPresentationModule` |

The user asked for `:feature:verblist`. Verb data and favourites will be read by details, tests and settings too, so
domain/data live in `core` (kmp-module-structure: shared by 2+ features → core). That is why the feature is a single
presentation module, not `:feature:verblist:{domain,data,presentation}`. `:shared` wires everything: Koin start, the
`App()` shell, the installer call. A JVM target is added to `:core:domain` and `:core:database` only, so the
bundled-database task (D3) can run the real Room classes on the build machine.

### D2. Verb database schema
- `verbs(slug PK, infinitive, group, reflexive, auxiliary, frequency, updated_at, infinitive_key, sort_key)`, with
  indices on `sort_key`, `infinitive_key` and `frequency`.
- `forms(id PK, slug FK, mood_code, mood_name, mood_pos, tense_code, tense_name, tense_pos, person, form_pos, conjugation
  JSON, root, suffix JSON, auxiliary, semi_auxiliary, infinitive_preposition, reflexive_pronoun, conjunction,
  preposition, pronoun JSON, spoken_text)`. This covers A-3, rebuilds *Get verb details* exactly, and is used by
  Screen 2 later.
- `translations(slug PK, language, definition, translation)`, `synonyms(slug, position, synonym_slug,
  synonym_infinitive)`.
- `form_search(key, slug, form, display_order)`, index `(key, slug)`. There is one row per distinct default spelling
  (index 0) of a *simple* form (simple tenses, Infinitif présent, participles). `display_order` is the D-3
  mood/tense/person rank used to pick the matched form. The infinitive is searched through `verbs.infinitive_key`, not
  this table.
- `metadata(key PK, value)`: `lastUpdatedAt`, `schemaVersion`, `bundleBuild` of the installed copy, and the saved
  checkpoint (A-1). It lives inside the verb DB, so a replaced DB carries its own values.

Simple tenses are taken from the API mood/tense codes: Indicatif PRESENT, IMPARFAIT, PASSE_SIMPLE, FUTUR_SIMPLE;
Conditionnel PRESENT; Subjonctif PRESENT, IMPARFAIT; Impératif PRESENT; Infinitif PRESENT; Participe PRESENT, PASSE.
Compound tenses, Gérondif and Constructions verbales are excluded. This matches the API's "only the verb word of simple
tenses, the infinitive and the participles" (the Gérondif's verb word equals the Participe présent anyway).

### D3. Bundled database build (A-1, A-2)
- Root `gradle.properties`: `frenchVerbsDataset.version=3`, `frenchVerbsDataset.sha256=01c88cde…72ac`.
- The token is read from `local.properties` key `frenchVerbsDataset.githubToken`, falling back to the environment
  variable `FRENCH_VERBS_DATASET_TOKEN` for CI. When it is missing, the build fails with "Missing
  frenchVerbsDataset.githubToken in local.properties (fine-grained token, Contents read-only on VAMVisionsKMP)".
- `:core:database` tasks:
  1. `downloadFrenchVerbsDataset` (cacheable, output `build/dataset/french-verbs-dataset-v3.json.gz`) resolves the
     release asset through the GitHub REST API (`/repos/AnnaMGh/VAMVisionsKMP/releases/tags/…`, asset `url` with
     `Accept: application/octet-stream`) and verifies the SHA-256 against the pin.
  2. `buildBundledVerbDatabase` is a `JavaExec` on the module's JVM compilation. It runs `BundledDatabaseBuilder.main`,
     which opens `VerbDatabase` with `BundledSQLiteDriver` on a file, streams the gzip JSON with kotlinx-serialization,
     maps it with the same mapper sync will use, writes `metadata`, runs `VACUUM`, and writes `verbs.db` plus
     `verbs-meta.json` (`lastUpdatedAt`, `schemaVersion`, `bundleBuild`, `attribution`, `datasetLicenseUrl`,
     `sources`).
- Outputs go to `:core:data` Compose resources (`files/verbs.db`, `files/verbs-meta.json`). They are registered with
  `compose.resources { customDirectory(sourceSetName = "commonMain", directoryProvider = <task output dir>) }`, so the
  task output feeds resource generation with proper task dependencies (configuration-cache safe). `:core:data` enables
  `androidResources` like `:core:designsystem`. This packs the files into the Android app and the iOS framework bundle
  without per-platform copy steps.

Alternative: commit the `.db` file. It was rejected by A-2 (history size, skips the checksum check).

### D4. Installing the bundled database
`BundledVerbDatabaseInstaller.installIfNeeded()` runs once from `:shared` before Koin creates `VerbDatabase` (the Room
builder is behind a `single` that first awaits the installer).
1. Read `verbs-meta.json` from resources (small).
2. Read the local copy's `metadata` with a raw `SQLiteConnection` (no Room) when the file exists.
3. If any A-1 condition holds, copy the bundled file by streaming. Never use `Res.readBytes`, which loads the whole
   file into memory. On Android, open the asset `InputStream` from the path given by `Res.getUri` through
   `AssetManager`. On iOS, copy the bundle file at that path with `NSFileManager`. Write `verbs.db.tmp`, fsync, then
   atomically rename it to `verbs.db`. Delete `-wal`/`-shm`. On iOS, set `NSURLIsExcludedFromBackupKey`.
4. Set the checkpoint to the bundled `lastUpdatedAt`.

Platform paths come from `PlatformPaths`, injected by Koin's `platformModule` (D12): Android
`context.getDatabasePath("verbs.db")`; iOS `Application Support/verbs.db`. The first-launch copy is excluded from the 1 s start target (N-2). The screen shows the
L-07 skeleton while the install runs.

Alternative: Room's `createFromAsset`. It was rejected because it is Android-only and has no replace-on-newer rule
(A-1).

### D5. Queries (verb-data spec)
The list query is built by a `ListQueryBuilder` into a `RoomRawQuery` (`@RawQuery(observedEntities = [...])` returning
`PagingSource<Int, VerbRowEntity>`), because filters, grouping and sort combine freely:
- **Search**: `q = normalizeKey(text.trim())`. Prefix match as an index range, `key >= :q AND key < :q || '￿'`, on
  `verbs.infinitive_key` UNION `form_search.key`, then grouped by slug. `matched_form` is NULL if the infinitive
  matched, otherwise the form with `key = :q` first, then the lowest `display_order` (a correlated subquery). This
  avoids `LIKE` without an index and meets N-2's 100 ms.
- **Filters**: `group IN (…)`; `auxiliary IN (…)`, where être maps to `('ETRE','BOTH')` and avoir to
  `('AVOIR','BOTH')`; `reflexive IN (…)`. Each category is skipped when empty.
- **Favourites tab**: `slug IN (:favouriteSlugs)`. Favourites are in another database, so the ViewModel passes the
  current set from `FavouritesRepository.observeSlugs()`. With ~hundreds of favourites at most, an `IN` list is fine.
  An `ATTACH` was rejected because it breaks Room's invalidation.
- **Group by**: section rank first (`group`; `auxiliary` ordered AVOIR, ETRE, BOTH; `reflexive` false, true). Then the
  sort: `sort_key` ASC/DESC or `frequency DESC`. Ties: `infinitive_key ASC, slug ASC`.
- Section headers are added with `PagingData.insertSeparators` when the section rank changes.

`VerbCatalog.isEmpty(): Flow<Boolean>` drives L-10.

### D6. User database and scores
`UserDatabase` v1 tables:
- `favourites(slug PK, added_at)`
- `test_attempts(id PK, slug, kind, started_at, selected_tenses JSON)`, with kind `TEST` or `REVIEW`. Review sessions
  (RV-4) will save their cards here as `REVIEW`. They count in mastery but never in the last test score, so adding
  them later needs no migration.
- `attempt_lines(attempt_id FK, mood_code, tense_code, person NULL, typed_text, result)`, with result one of `CORRECT`,
  `CORRECT_NO_ACCENTS`, `FAILED`

The schema is exported (`room { schemaDirectory }`) so later versions can auto-migrate. Last score per slug is
`ROUND_HALF_UP(100 × correct / total)` over the latest attempt, computed in Kotlin from `(correct, total)` counts that
Room returns. Only the latest attempt with `kind = TEST` counts. `observeScores(slugs)` returns a `Flow<Map<String, Int>>` for the visible page's slugs.

Hiding removed verbs: favourites count = `favourites.slug ∈ verbs.slug`. Because the two DBs are separate, the
repository combines `observeSlugs()` with `VerbCatalog.existingSlugs(slugs)`. No row is deleted.

### D7. App settings
DataStore Preferences (KMP, `okio` path from `PlatformPaths.dataStoreFile`, D12), file `app_settings.preferences_pb`. Keys:
`list.filters.groups`, `list.filters.auxiliaries`, `list.filters.reflexive` (string sets), `list.groupBy`, `list.sort`,
`list.tab`. The search text is never written.

### D8. Window layout classification
The adaptive structure follows Notion N-15 thresholds, not the skill's default size classes:

| `WindowLayout` | Rule | Screen 1 structure |
|---|---|---|
| `Compact` | width < 600 dp | bottom bar; top bar row, tabs row, choices row, 1 column |
| `PhoneLandscape` | width ≥ 600 and height < 480 | rail; one top row (search · tabs · controls), choices row, 2 columns |
| `ListDetail` | width ≥ 600 and height ≥ 480 | rail; 360 dp list pane (portrait-style rows) · divider · right pane |

`currentWindowLayout()` in `:core:presentation` reads `currentWindowAdaptiveInfo().windowSizeClass` (material3-adaptive)
with explicit dp breakpoints. The shell in `:shared` picks bar vs rail. `VerbListScreen` picks its own structure, so
this is a one-off adaptive layout for Screen 1, kept in the feature. No other screen shares it yet; it moves to
`:core:designsystem` when Screens 3/6 need list–detail. "Back clears the selection on Medium width" uses the
common `BackHandler` of Compose Multiplatform 1.12, enabled only in `ListDetail` on Medium width when a selection
exists.

### D9. Screen composition
- One `LazyVerticalGrid(columns = Fixed(1 or 2))` with `stickyHeader` for sections (verified: foundation 1.12.1
  `LazyGridScope.stickyHeader` exists). Section headers span the full line, so PL-01's full-width headers need no
  special case. Items use `key = slug`, `contentType` per row type.
- The grid state is hoisted in the Root via `rememberLazyGridState` and saved. Rotating keeps the first visible index.
  Coming back to the list keeps the same state (the list stays in composition behind the future navigation).
- The top bar uses `VerbeClairSearchBar` (`weight(1f)`) + 4 dp gap + `VerbeClairIconButton` (Standard, or Tonal with
  badge when filters > 0). In `PhoneLandscape` the same row also holds `ListTabs` (272 dp in PL-01). Elsewhere
  `ListTabs` is its own row.
- `ActiveChoicesRow`: a horizontally scrolling `Row` of `VerbeClairFilterChip(selected = true)`, padding 16 dp
  horizontal, 8 dp bottom, 8 dp gap (10:320).
- `ListControlsSheet`: `ModalBottomSheet` on `surface-container-low`, top radius `xl`, `BottomSheetHandle`, header
  "List options" (Title/Large) + Text `VerbeClairButton` "Reset". Overlines in Label/Overline `primary`, field labels in
  Title/Small `on-surface-variant`, chips in a `FlowRow` with 8 dp gaps, a divider, Group by chips (single-select),
  Sort `RadioRow`s, padding as in 10:519. `sheetMaxWidth = 640.dp`.
- Empty states use `EmptyState(Neutral)` with icons from `VerbeClairIcons`: search-off (L-05), cloud-download (L-06),
  star (L-09), book-open (tablet empty pane, chosen by the user on 2026-10-16). `VerbeClairLinearProgress` sits under the tabs in L-06
  while `isSyncing`.
- Skeleton delay: the screen shows `VerbCellSkeleton` rows for `loadState.refresh is Loading` only after a 150 ms
  `LaunchedEffect` delay (N-2), and for appended placeholders.

### D10. ViewModel (MVI)
`VerbListViewModel(catalog, favourites, scores, preferences, syncTrigger)`:
- State: `query`, `choices: ListChoices` (filters, groupBy, sort), `tab`, `favouritesCount`, `isSheetOpen`,
  `selectedSlug`, `isEmptyDatabase`, `isSyncing`. Paging is exposed separately as `verbs: Flow<PagingData<VerbListRowUi>>`
  (`cachedIn(viewModelScope)`), because `PagingData` doesn't belong in a data class state.
- The query text is debounced 300 ms (`debounce` on a `MutableStateFlow`). `combine(debouncedQuery, choices, tab,
  favouriteSlugs)` → `flatMapLatest { catalog.pagedList(it) }`. Scores and favourite flags are joined per page with
  `combine(pagingFlow, favouriteSlugs, scores)` using `PagingData.map`.
- Actions: `OnQueryChange`, `OnClearQuery`, `OnTabSelect`, `OnOpenSheet`/`OnDismissSheet`, `OnToggleGroup/Auxiliary/
  Reflexive`, `OnGroupBySelect`, `OnSortSelect`, `OnReset`, `OnRemoveChoice(ActiveChoice)`, `OnClearFilters`,
  `OnToggleFavourite(slug)`, `OnSelectVerb(slug)`, `OnClearSelection`. Cell taps and test taps call the navigation
  callbacks directly from the Screen (no Event type is needed). `OnSelectVerb` only updates `selectedSlug` for the
  tablet highlight.
- Choices and tab are loaded from `ListPreferences` at start and written back on each change.
- When `isEmptyDatabase` is true, `syncTrigger.requestSync()` is called once. It is a no-op in this change.

### D11. Labels and strings
`VerbListItem.toUi()` maps enums to string resources (labels table), giving `meta = "3rd group · être · non-reflexive ·
very common"`. Frequency thresholds are ≥100, ≥10, ≥1, else rare. The score is formatted as "80%". All screen strings
are in `:feature:verblist` `composeResources/values{,-fr,-ro}/strings.xml`. fr/ro are drafted and flagged for the
user's review, as in the previous change.

### D12. Koin and app host
Koin is set up first (task 1.3), before any binding exists. Each later task only adds definitions to its own module and
extends the graph test.
- **Modules**, one per layer (android-di-koin):
  - `platformModule` is an `expect val` in `:core:data`. Its `actual`s bind `PlatformPaths`
    (`verbDatabaseFile`, `userDatabaseFile`, `dataStoreFile`).
    - Android: `AndroidPlatformPaths(androidContext())`.
    - iOS: `IosPlatformPaths`, using `NSFileManager` Application Support and Documents directories.
  - `coreDataModule`: the databases, DataStore, repositories, installer and `DataSyncTrigger`.
  - `verbListPresentationModule`: `viewModelOf(::VerbListViewModel)`.
  - Constructor-reference forms (`singleOf`, `viewModelOf`) are used, except for the Room/DataStore builders.
- **Start:** `:shared` exposes `val appModules = listOf(platformModule, coreDataModule, verbListPresentationModule)`
  and `fun initKoin(config: KoinAppDeclaration = {})`, which calls `startKoin { config(); modules(appModules) }`.
  - Android: a new `VerbeClairApplication` (in the manifest) calls `initKoin { androidContext(this@VerbeClairApplication) }`.
  - iOS: `MainViewController` calls `initKoin()` once, guarded so that recreating the view controller does not start
    Koin twice.
- **Graph test:** `AppKoinGraphTest` in `:shared` `commonTest` runs as an Android device test and on the iOS
  simulator.
  - It starts `koinApplication { modules(appModules + testOverrides) }`, where `testOverrides` points `PlatformPaths`
    at a temporary directory.
  - Android adds `androidContext` from the instrumentation context through a small `expect`/`actual` test helper.
  - It resolves every public binding, ViewModels included, so a missing or mistyped definition fails on both
    platforms before the app starts.
  - Koin's `verify()` is JVM-only reflection, so it can't cover iOS, which is why it isn't used.
- `App()` is replaced by `VerbeClairTheme { AppShell() }`. `AppShell` renders bar/rail by `WindowLayout` with Verbs
  selected and `VerbListRoot(onVerbClick = {}, onTestClick = {})`. The empty lambdas are the placeholders the details
  and test changes replace. Tapping Tests/Settings keeps Verbs selected; there is no destination yet.
- The Greeting template files are removed.

### D13. New screen-level pieces and theming
Two new composables are needed, and neither exists in Figma Components (3:64) or in `:core:designsystem`:
- `ListControlsSheet`: Figma has the sheet only as a frame inside L-04 (10:519), not as a component set.
- `ActiveChoicesRow`: the previous change listed it as screen layout (`add-verb-list-components` proposal, "Not in
  this change").

Both are composed only from existing components (`BottomSheetHandle`, `VerbeClairButton`, `VerbeClairFilterChip`,
`RadioRow`) and theme tokens. They live in `:feature:verblist` because only Screen 1 uses them. The tablet right pane
reuses `EmptyState`.

**Theming:** `VerbeClairTheme(darkTheme = isSystemInDarkTheme())` wraps `AppShell`, so the whole screen follows the
system appearance (N-5). Colours, type, spacing and radii come only from `MaterialTheme` / `VerbeClairTheme` tokens.
The flag stripes come from `flagScreenBackground()` (content), `flagBarBackground()` (bottom bar) and
`flagRailBackground()` (rail). Nothing is hard-coded. Every preview is rendered in both
`VerbeClairTheme(darkTheme = false / true)`.

## Risks / Trade-offs

- [Resolving the verb database in the graph test runs the installer and copies the bundled file] → `testOverrides`
  points `PlatformPaths` to a temp directory, and the test resolves `VerbDatabase` once and closes it. It runs in a few
  seconds on the emulator.

- [Room KMP `@RawQuery` returning a Paging `PagingSource` on iOS is not yet verified against the pinned Room version]
  → Check it in task 1.1/4.2 before building on it. Fallback: a small set of typed `@Query` methods (one per sort ×
  groupBy, with filter lists as `IN` parameters and `:applyX` flags), or a custom LIMIT/OFFSET `PagingSource` over
  `RoomRawQuery` that registers an invalidation tracker observer.

- [Build needs a token and network] → Clear failure message naming the property. The download is cached per version,
  so offline rebuilds work after the first.
- [Room JVM target in `:core:database` only for the build task] → Keep the JVM target free of app code. The task
  depends only on the JVM compilation of `:core:domain` and `:core:database`.
- [Compose resources hold a ~25 MB uncompressed DB] → Resources are compressed in the APK (about 8 MB). Copy is
  streamed, never read fully into memory. Measure APK and IPA size in the PR.
- [Search query cost with UNION + correlated subquery] → `form_search` is indexed on `key`. Add an androidHostTest-free
  instrumented benchmark-style test asserting "su" returns within budget on the full bundled DB. Fall back to a
  precomputed `(key, slug, best_form)` table if needed.
- [Two databases cannot be joined in SQL] → The favourites `IN` list and the existence check are done in the
  repository. Fine at expected sizes.
- [Empty-state body is required by `EmptyState`] → The tablet pane has a title only. Make `body` optional (delta on
  `design-system-components`, Empty state requirement).

## Migration Plan

- `UserDatabase` starts at version 1 with an exported schema. Later changes add versions with auto-migrations.
  `VerbDatabase` never migrates: a schema change raises `VERB_SCHEMA_VERSION` and the bundled DB replaces the local one
  (A-1).
- Rollback: revert the PR. There is no released app yet, so no on-device data to preserve.

## Open Questions

- French and Romanian copy for the new strings is drafted and needs the user's review in the PR.
