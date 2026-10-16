## Why

Screen 1 (Verb list) is the screen VerbeClair opens on. Its design-system components now exist
(`add-verb-list-components`), but the app still shows the template "Click me!" screen, and it has no verb data at all:
no local database, no bundled dataset, no favourites. Without a local, offline data layer the list can't show the
7,092 verbs, search them, filter them or mark favourites (L-1…L-15, A-1…A-4, A-7, N-1). This change combines the
`add-verb-data-layer` and `add-verb-list-screen` steps planned in the previous change into one, as decided by the user
on 2026-10-16.

## What Changes

- **Bundled verb database (A-1, A-2):** a Gradle task downloads the pinned dataset release
  `french-verbs-dataset-v<N>.json.gz` from the private VAMVisionsKMP GitHub repository, using a fine-grained,
  read-only token from `local.properties`. It checks the file against the SHA-256 pinned in `gradle.properties`, then
  builds the Room verb database in a JVM task with the app's own `@Database` classes and mapping code, plus a metadata
  file (`lastUpdatedAt`, schema version, `bundleBuild`, licence attribution and sources). Both files are packed into
  the Android assets and the iOS bundle. At start-up a shared `commonMain` rule installs the bundled file when there is
  no local database, or when the bundled data is newer, the schema differs or `bundleBuild` is higher. The copy goes
  to a temp file that is then renamed. On iOS the database lives in Application Support, excluded from iCloud backup.
- **Local verb schema and keys (A-3, A-4):** verbs, forms (all parts, variants, position), translations and
  synonyms, plus normalised search and sort keys built by one shared normaliser.
- **Local list queries (L-1, L-3…L-6):** paged list (Room + Paging 3), prefix search on the infinitive and on the
  default spelling of simple forms, matched form (L-4), filters (OR within a category, AND across categories; `BOTH`
  matches avoir and être), group by, and sort with ties broken by the normalised infinitive and then the slug.
- **User database (A-7):** a second Room database, linked to verbs only by `slug`, holding favourites plus the test
  attempt and line tables (T-9 shape). Those tables make the last test score (L-14) a real query. Nothing writes
  attempts yet; the Test change will. Data of a removed verb is hidden, not deleted.
- **App-settings store:** DataStore Preferences (KMP) keeps the list choices (filters, group by, sort) and the
  selected tab across launches (L-7, L-12). The search text is not kept.
- **Verb list screen (`:feature:verblist`):** MVI ViewModel, Root/Screen composables and every Figma state. The
  states are default, search with matched forms, grouped with sticky headers and active choices row, list controls
  sheet with badge and Reset, empty result, no data, loading skeleton, Favourites tab and no favourites. The screen
  also covers phone landscape (rail, one top row, 2-column list) and tablet list–detail (360 dp list, right pane with
  the "Choose a verb to see its conjugation" empty state).
- **Navigation hooks only:** tapping a cell and the test button report `onVerbClick(slug)` / `onTestClick(slug)`.
  Verb details (Screen 2) and test setup (Screen 4) come in their own changes. On tablets the selected cell is
  highlighted and the right pane keeps its empty state until Screen 2 exists.
- **App host:** `App.kt` replaces the template content with the verb list inside a minimal shell. The shell shows the
  bottom bar on compact width and the navigation rail at 600 dp or wider (NV-1, NV-4), with Verbs selected. Tests and
  Settings are not built yet.
- **New modules:** `:core:domain`, `:core:database`, `:core:data`, `:core:presentation`, and `:feature:verblist`
  (presentation). Verb data and favourites are shared with later features (details, tests), so they live in `core`.
- Not in this change: network API client, sync and build flavors (SY-*, A-5, A-9), so the `prodRelease` "newer
  dataset available" check of A-2 is also deferred. L-10's "trigger a sync" becomes a hook that does nothing until
  sync exists. Also not included: verb details, test setup, Tests and Settings screens, and per-tab back stacks
  (NV-2).

## Capabilities

### New Capabilities
- `verb-data`: the offline verb catalogue. Covers the bundled database build and install (A-1, A-2), the local
  schema (A-3), normalised keys (A-4), and the list, search, matched-form, filter, group and sort queries the verb
  list uses (L-1, L-3…L-5).
- `user-data`: the user database (A-7). Covers favourites (add, remove, count, hidden for removed verbs), the test
  attempt and line tables, and the last test score query (L-13, L-14), plus the app-settings store for remembered
  list choices and tab (L-7, L-12).
- `verb-list-screen`: Screen 1. Covers the top bar, tabs, cells, list controls sheet, active choices row, empty, no
  data and loading states, favourite and test buttons, phone portrait, phone landscape and tablet layouts, and light
  and dark mode (L-1…L-15, NV-1, NV-4, N-2, N-14, N-15).

### Modified Capabilities
- `design-system-components`: the Empty state's body becomes optional, for the tablet right pane, which has only a
  title ("Choose a verb to see its conjugation"). All other component requirements are reused unchanged.

## Requirements and design sources

- Notion: [VerbeClair requirements](https://app.notion.com/p/VerbeClair-requirements-3f1d02620bc1804180a7f241233fdacb):
  section 6 (L-1…L-15 and the labels table), A-1…A-4, A-7, NV-1, NV-3, NV-4, N-1, N-2, N-4, N-5, N-6, N-14, N-15.
  [VerbeClair API](https://app.notion.com/p/VerbeClair-API-3f1d02620bc18082b1d7d0bad9581b31) for the data shape
  (*Get verb details*, *How search works*, enums).
- Figma, [Screens (3:65)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-65):
  - L-01 Default: [light 10:2](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-2), [dark 13:913](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-913)
  - L-02 Search "su": [light 10:138](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-138), [dark 13:936](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-936)
  - L-03 Grouped + filters: [light 10:259](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-259), [dark 13:957](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-957)
  - L-04 List controls sheet: [light 10:405](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-405), [dark 13:985](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-985)
  - L-05 Empty result: [light 10:595](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-595), [dark 13:1045](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-1045)
  - L-06 No data: [light 10:687](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-687), [dark 13:1057](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-1057)
  - L-07 Loading skeleton: [light 10:762](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-762), [dark 13:1070](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-1070)
  - L-08 Favourites tab: [light 55:3940](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=55-3940), [dark 55:4034](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=55-4034)
  - L-09 No favourites: [light 55:4047](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=55-4047), [dark 55:4119](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=55-4119)
  - PL-01 Landscape: [light 87:9531](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-9531), [dark 90:12103](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=90-12103)
  - TP-01 Tablet portrait (list pane): [light 88:744](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=88-744), [dark 90:12250](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=90-12250)
  - TL-01 Tablet landscape (list pane): [light 89:1153](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=89-1153), [dark 90:12336](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=90-12336)
  - Adaptive rules note [87:7](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=87-7)
- Figma, [Components (3:64)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-64),
  all reused from `:core:designsystem`: Search bar, Icon button, List tabs, Verb cell, Section header, Verb cell
  skeleton, Empty state, Chip, Radio row, Button, Progress/Linear, Bottom sheet/Handle, Bottom nav, Navigation rail.
- Decided by the user on 2026-10-16 (not specified in Notion or Figma):
  - scope is the UI plus the local data layer, with callbacks for details and test;
  - the screen lives in a new `:feature:verblist` module;
  - the tablet empty pane reuses the Neutral `EmptyState` (no dedicated Figma frame);
  - the user adds the dataset token to `local.properties`;
  - the attempt tables are created now;
  - DataStore Preferences is the app-settings store.

## Impact

- **New modules:** `:core:domain`, `:core:database` (two Room databases, KSP), `:core:data` (repositories, bundled DB
  installer, DataStore), `:core:presentation` (window classification, `UiText`), `:feature:verblist`. Changed:
  `settings.gradle.kts`, `:shared` (`App.kt` and the Koin start), `:androidApp` (Koin and Android context),
  `iosApp` (bundle resources).
- **New dependencies** in `gradle/libs.versions.toml`: Room KMP + KSP + bundled SQLite, Paging 3 (KMP), DataStore
  Preferences, Koin, kotlinx-serialization, kotlinx-coroutines, material3-adaptive / window-core, Turbine (tests).
- **Build:** a new JVM Gradle task builds the bundled DB. `./gradlew build` now needs network access the first time
  for each dataset version, plus a valid GitHub token in `local.properties`. A missing or rejected token fails the
  build with a message naming the property. CI will need the same secret.
- **App size:** about 8 MB more (compressed bundled database).
- **No API calls:** the app makes no network calls at runtime in this change.
