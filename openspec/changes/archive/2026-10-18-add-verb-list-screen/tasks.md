Figma base URL used below: `F = https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=`. In the code, every `// Figma:` comment MUST hold the full URL, e.g. `F10-2` → `...?node-id=10-2`.

Before each UI task, re-read its frames with `get_design_context` (figma-design-to-code skill loaded) and `get_variable_defs`. Never implement from a screenshot. Screen previews are in light and dark and are checked with `android-preview-figma-verify` against the frame in their `// Figma:` comment.

**Prerequisite (user):** create the fine-grained GitHub token (Contents read-only on `VAMVisionsKMP`) and add it to `local.properties` as `frenchVerbsDataset.githubToken` before task 3.2.

## 1. Modules and dependencies

- [x] 1.1 Add to `gradle/libs.versions.toml` (android-version-catalog skill, latest stable versions compatible with Kotlin 2.4.20 / CMP 1.12.1): Room KMP (`room-runtime`, `room-compiler`, `room-paging`, gradle plugin), KSP, `sqlite-bundled`, Paging (`paging-common`, `paging-compose`), DataStore Preferences, Koin (core, compose, compose-viewmodel, android), kotlinx-serialization-json + plugin, kotlinx-coroutines (core, test), material3-adaptive, window-core, Turbine. Verify: `./gradlew help` resolves the catalog.
- [x] 1.2 Create `:core:domain`, `:core:database`, `:core:data`, `:core:presentation`, `:feature:verblist` with plain build files modelled on `core/designsystem/build.gradle.kts` (design D1; JVM target only on `:core:domain` and `:core:database`), and include them in `settings.gradle.kts`. Verify: `./gradlew :core:domain:assemble :core:database:assemble :core:data:assemble :core:presentation:assemble :feature:verblist:assemble` succeeds.

- [x] 1.3 Set up Koin (D12):
  - `expect val platformModule` in `:core:data` with `PlatformPaths` actuals for Android (`androidContext()`) and iOS
    (Application Support / Documents)
  - `appModules` and `initKoin()` in `:shared`
  - `VerbeClairApplication` registered in the `:androidApp` manifest, calling `initKoin { androidContext(...) }`
  - the guarded `initKoin()` in `MainViewController`
  - `AppKoinGraphTest` in `:shared` `commonTest`, with the temp-dir `testOverrides` and the Android `androidContext`
    test helper

  Verify:
  - the app still starts on Android and the iOS simulator with Koin running
  - `AppKoinGraphTest` passes on both (`:shared:connectedAndroidDeviceTest`, `:shared:iosSimulatorArm64Test`) and
    resolves `PlatformPaths`
  - a deliberately missing binding makes it fail (checked once, then reverted)

## 2. Domain

- [x] 2.1 Add the domain models and interfaces of D1 (`VerbListItem`, `ListQuery`, `ListChoices`, enums, `VerbCatalog`, `FavouritesRepository`, `TestScoreRepository`, `ListPreferences`, `DataSyncTrigger`). Verify: `:core:domain` compiles for all targets.
- [x] 2.2 Implement `normalizeKey()` and `sortKey()` (A-4). Verify: `commonTest` unit tests — "Être" → "etre", "œuvrer" → "oeuvrer", "s’appeler" → "s'appeler", sort key of "s'appeler" = "appeler", "se " prefix dropped, accents removed for every French accented letter.
- [x] 2.3 Implement the last-score rule (half up, CORRECT_NO_ACCENTS counts as correct) as a pure function. Verify: unit tests — 2/3 → 67, 79.5% → 80, 8/10 → 80, 9 correct + 1 no-accent → 100.

## 3. Verb database and bundled build

- [x] 3.1 Implement `VerbDatabase` entities, DAOs and `metadata` (D2), and the dataset JSON model plus the dataset → entity mapper that fills `form_search` with simple-form keys only. Verify: `commonTest` mapper tests on fixtures from the API page — terminer, être (`suis` in `form_search`), aller (passé composé `allé`/`allée` NOT in `form_search`; futur proche `vais` NOT in it), payer (two `je` présent rows), s'appeler (sort key `appeler`). Also a round-trip test: entities → rebuilt *Get verb details* JSON equals the fixture.
- [x] 3.2 Add `downloadFrenchVerbsDataset` (D3): pin in `gradle.properties`, token from `local.properties` / `FRENCH_VERBS_DATASET_TOKEN`, SHA-256 check, cached under `build/dataset/`. Verify: the task succeeds with the token. With a wrong pinned checksum it fails with a checksum message. Without the token it fails naming `frenchVerbsDataset.githubToken`. A second run is UP-TO-DATE.
- [x] 3.3 Add `buildBundledVerbDatabase` (JVM `JavaExec`, D3) writing `verbs.db.gzip` (gzip of the vacuumed database) and `verbs-meta.json`, registered in `:core:data` with `compose.resources { customDirectory(...) }`. Verify: only those two files are in the output; a JVM test decompresses the built file, opens it with Room and checks 7,092 verbs, `lastUpdatedAt = 2026-10-08T09:26:22Z`, and the schema identity hash equals the runtime database's.

## 4. Data layer

- [x] 4.1 Implement `BundledVerbDatabaseInstaller` with `expect`/`actual` file access (D4): the four A-1 conditions, streamed okio `GzipSource` decompression of `verbs.db.gzip`, temp-file + rename, checkpoint update, iOS Application Support + `NSURLIsExcludedFromBackupKey`. Verify: `commonTest` instrumented tests — no local DB → installed; local newer + same schema/build → kept; different schema → replaced; higher `bundleBuild` → replaced; the installed file is the decompressed database (Room opens it); a leftover `verbs.db.tmp` from a killed install is discarded and the install redone; the user DB file is untouched. iOS: a test asserts the backup-exclusion flag.
- [x] 4.2 First confirm Room `@RawQuery` → `PagingSource` works on iOS with the pinned Room version (else use the D5 fallback in Risks), then implement `ListQueryBuilder` and `RoomVerbCatalog.pagedList()` / `isEmpty()` / `existingSlugs()` (D5). Verify: instrumented tests on an in-memory `VerbDatabase` seeded with a fixture set —
  - "su" → être (suis), savoir (su), suivre (no form)
  - "ete" → être (été)
  - "va" and "parlée" don't return parler
  - "arl" returns nothing
  - blank → all
  - être filter includes passer (BOTH)
  - groups 1+2 → no 3rd group
  - groupBy group + frequency → 1st group first, most frequent first
  - Z→A order
  - ties by infinitive key then slug
  - each slug at most once
- [x] 4.3 Implement `UserDatabase` v1 (favourites, test_attempts, attempt_lines, exported schema) and `RoomFavouritesRepository` / `RoomTestScoreRepository` (D6). Verify: instrumented tests — add/remove/count; a favourite whose slug is missing from the verb DB is hidden from the count and list but still stored, and reappears when the verb is present; the latest attempt drives the score, whether a test or a review session (a later review session of 2 tenses, 9 of 12 lines → 75); no attempt → no score.
- [x] 4.4 Implement `DataStoreListPreferences` (D7) and `NoOpDataSyncTrigger`. Verify: tests — choices and tab written then read back from a new DataStore instance on the same file; search text is not a stored key.
- [x] 4.5 Add the `coreDataModule` bindings to the Koin setup from 1.3: databases (verb DB opened only after the installer), DataStore, repositories and `DataSyncTrigger`, with paths from `PlatformPaths`. Verify: `AppKoinGraphTest` resolves `VerbCatalog`, `FavouritesRepository`, `TestScoreRepository`, `ListPreferences` and `DataSyncTrigger` on Android and iOS.
- [x] 4.6 Performance check on the full bundled DB (instrumented): first page query < 1 s after open, "su" search < 100 ms. Verify: test asserts the budgets on the emulator. Record the timings in the PR.

## 5. Presentation foundations

- [x] 5.1 Implement `WindowLayout` + `currentWindowLayout()` in `:core:presentation` with the N-15 thresholds (D8). Verify: unit tests — 393×852 → Compact, 852×393 → PhoneLandscape, 834×1194 and 1194×834 → ListDetail, 599 wide → Compact, 700×479 → PhoneLandscape.
- [x] 5.2 Make `EmptyState.body` optional in `:core:designsystem` (delta spec). Add a preview without body `// Figma: F7-135`. Verify: UI test — no body node when `body = null`. Existing EmptyState previews and tests still pass.

## 6. Verb list ViewModel

- [x] 6.1 Implement `VerbListState`, `VerbListAction`, UI models and the labels mapping (D10, D11), with en/fr/ro strings. Verify: unit tests — aller → "3rd group · être · non-reflexive · very common"; BOTH → "avoir or être"; frequency 100 / 99.9 / 10 / 1 / 0.99 / 0 → very common / common / common / uncommon / rare / rare.
- [x] 6.2 Implement `VerbListViewModel` (debounce, combine to paging, favourites, scores, preferences, sync request when empty) with fakes of the domain interfaces. Verify: unit tests with Turbine —
  - "s","su","sui" within 300 ms → one query for "sui"
  - L-6 sequence: être + non-reflexive + by group + most frequent → badge 2 and 4 active chips; remove "être" → badge 1; remove "Most frequent" → sort A→Z
  - Reset → defaults and no chips
  - Clear filters → filters and search cleared, grouping and sort kept
  - tab and choices restored from preferences; search not restored
  - toggling a favourite calls the repository
  - empty database → `requestSync()` called once

## 7. Verb list screen

- [x] 7.1 Implement `VerbListScreen` for `Compact`: top bar, tabs, active choices row, grid with sticky section headers, cells with score/test/star, skeleton after 150 ms, and the empty-result, no-data (+ progress while syncing) and no-favourites states. Previews:
  - L-01 Default: `// Figma: F10-2` (light), `// Figma: F13-913` (dark)
  - L-02 Search "su": `// Figma: F10-138`, `// Figma: F13-936`
  - L-03 Grouped + 2 filters: `// Figma: F10-259`, `// Figma: F13-957`
  - L-05 Empty result: `// Figma: F10-595`, `// Figma: F13-1045`
  - L-06 No data: `// Figma: F10-687`, `// Figma: F13-1057`
  - L-07 Skeleton: `// Figma: F10-762`, `// Figma: F13-1070`
  - L-08 Favourites tab: `// Figma: F55-3940`, `// Figma: F55-4034`
  - L-09 No favourites: `// Figma: F55-4047`, `// Figma: F55-4119`

  Verify: UI tests —
  - tapping a cell calls `onVerbClick("terminer")`
  - tapping the test button calls only `onTestClick`
  - tapping the star calls only the favourite action
  - clear button empties the search
  - Clear filters button present only in the empty result
  - no button in the no-data and no-favourites states
  - skeleton not shown for a load faster than 150 ms
- [x] 7.2 Implement `ListControlsSheet` (`ModalBottomSheet`, max 640 dp) with Filter / Group by / Sort / Reset (D9). Previews: L-04 `// Figma: F10-405` (light), `// Figma: F13-985` (dark). Verify: UI tests —
  - chips toggle multi-select within a category
  - Group by and Sort are single-select
  - Reset restores defaults
  - the sheet is at most 640 dp wide in a 1194 dp window
- [x] 7.3 Implement the `PhoneLandscape` structure: one top row (search · tabs · controls), 2-column grid, full-width sticky headers. Previews: PL-01 `// Figma: F87-9531` (light), `// Figma: F90-12103` (dark), plus a grouped variant in landscape. No frame exists for it; it uses `// Figma: F87-9531` and a note that PL-01's annotation asks for full-width sticky headers. Verify: UI test — at 852×393 the grid has 2 columns and a section header spans both.
- [x] 7.4 Implement the `ListDetail` structure: 360 dp list pane, divider, right pane with `EmptyState` "Choose a verb to see its conjugation" (book-open icon, no body), selected cell highlight, `BackHandler` clearing the selection on Medium width. Previews: TP-01 `// Figma: F88-744` (light), `// Figma: F90-12250` (dark); TL-01 `// Figma: F89-1153` (light), `// Figma: F90-12336` (dark). Verify: UI tests — tapping a cell marks it selected and calls `onVerbClick`; the right pane shows the empty state; back clears the selection.
- [x] 7.5 Implement `VerbListRoot` (koinViewModel, `collectAsStateWithLifecycle`, `collectAsLazyPagingItems`, hoisted saved grid state) and add `verbListPresentationModule` to `appModules`. Verify: `AppKoinGraphTest` resolves `VerbListViewModel` on Android and iOS; UI test with a real ViewModel over an in-memory DB — typing "su" shows être with "suis"; rotating (configuration change) keeps the query and the first visible item.

## 8. App host

- [x] 8.1 Make sure app start-up runs the bundled-database installer before the verb DB is first resolved (Koin was set up in 1.3). Verify: the app starts on Android and on the iOS simulator with no Koin error, and the verb list loads from the installed DB.
- [x] 8.2 Replace `App()` with `AppShell`: bottom bar on `Compact`, rail otherwise, Verbs selected, `VerbListRoot(onVerbClick = {}, onTestClick = {})`, flag backgrounds. Delete the Greeting template. Verify: UI test — 393 dp shows the bottom bar, 852×393 shows the rail; a fresh install in flight mode shows the list (A-1).
- [x] 8.3 Pack the bundled resources into the iOS app and confirm the installer finds them. Verify: on the iOS simulator, a fresh install lists verbs offline, and the DB file is in Application Support with the backup-exclusion flag.
- [x] 8.4 Exclude the replaceable verb database from Android backup (D4): add `data_extraction_rules.xml` (API 31+, cloud backup and device transfer) and `backup_rules.xml` (`fullBackupContent`, API 24–30) to `:androidApp`, referenced from the manifest. Both exclude `database/verbs.db` and its `-wal`, `-shm`, `-journal` and `.tmp` files, and keep `user.db` and the DataStore file. Verify: `./gradlew :androidApp:lintDebug` reports no backup-rule issues; on the emulator with the local backup transport, `adb shell bmgr backupnow com.vamvisions.verbeclair` succeeds after the verb database is installed, and the backup holds `user.db` but not `verbs.db`.

## 9. Configuration and accessibility checks

- [x] 9.1 Check every screen preview above in light and dark with `android-preview-figma-verify`, and the running app with `android-ui-screenshot-verify` on a phone portrait, a phone landscape and a tablet emulator. Verify: no BLOCKER/MAJOR differences left; minor differences recorded in the PR.
- [x] 9.2 Check the largest system font size (`fontScale = 2f` previews of L-01 and L-02) and TalkBack labels (star, test button, clear search, list controls with badge count). Verify: no cut or overlapping text; labels match the spec.
- [x] 9.3 Check French and Romanian (`locale = "fr"` / `"ro"` previews of L-03 and L-04). Verify: no cut text; list the drafted fr/ro strings in the PR for the user's review.

## 10. Done gate

- [x] 10.1 Run `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew build` from the repository root (covers every new module, `:core:designsystem`, `:shared`, `:androidApp` and the iOS targets) and `./gradlew connectedAndroidDeviceTest` on a running emulator. Verify: both succeed, with the command output recorded.
