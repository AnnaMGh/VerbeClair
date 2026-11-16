## Why

The three static checks (ktlint, detekt and Konsist) were set up for an Android-only layout and quietly check less than they appear to on this Kotlin Multiplatform project. Konsist counts only a source set called `main` as production code, so about 99 files in `commonMain`, `androidMain`, `iosMain` and `jvmMain` are treated as test code and most architecture rules pass without looking at them. The detekt and `.editorconfig` test patterns miss `commonTest`, `iosTest`, `androidHostTest` and `androidDeviceTest`. `ktlintCheck` is not part of `check`, so CI's `./gradlew build` never runs it. Coverage is measured for two modules but nothing enforces it. These gaps must be closed before the PRISM tooling around the checks is removed (the next change, `remove-prism`), so that failures are not mixed with a large rename.

## What Changes

- **Konsist scope:** production code is every source set whose name ends in `Main`; test code is every source set whose name ends in `Test`. `ScopeIntegrityTest` fails if a module that has production code has none in scope. Rules that start failing on code they used to skip are fixed, or allow-listed one by one with a reason; none is silenced wholesale.
- **detekt and `.editorconfig` patterns:** every pattern that names `test` or `androidTest` also names `commonTest`, `iosTest`, `androidHostTest` and `androidDeviceTest`, so test-only rules apply to KMP tests and production-only rules skip them.
- **ktlint in `check`:** each module's `check` runs its `ktlintCheck`, so `./gradlew build` (and so CI) fails on a formatting violation.
- **Coverage ratchet:** each module that has a JVM or Android host test target gets a `jacocoCoverageVerification` task with a floor equal to today's measured coverage (`:core:database` 58 %, `:core:domain` 44 %). The target for every module is 80 %, kept as one constant. A floor may be raised but not lowered without a stated reason in the pull request. The `:tooling:*` build-tooling modules are plain Kotlin/JVM, are not measured by the plugin and are exempt by name. Modules with no JVM or host test target (`:core:data`, `:core:designsystem`, `:core:presentation`, `:feature:verblist`, `:androidApp`) are exempt and listed, until they get one. `:shared` has a host test target, so it has a floor (0 % today: its only host test is a placeholder).
- **CI:** the checks and unit tests job also runs `jacocoCoverageVerification`.
- **Typed results (Notion N-17, added 2026-11-09):** a data-layer function that can fail returns `Result<T, DataError>` or `EmptyResult<DataError>`. `Result`, `EmptyResult` and `DataError` are added to `:core:domain`; `FavouritesRepository.add` and `remove` return `EmptyResult<DataError.Local>`, which clears the Konsist finding "repository functions return a typed result".
- **Previews live with their composable:** a new detekt rule, `PreviewInSameFileAsComposable`, fails a `@Preview` that calls no non-private composable of its own file (a private frame used only by previews does not count). The 18 separate `*Previews.kt` files were merged into the files of the composables they preview.
- **Failed favourite change message:** the verb list shows a Snackbar (Figma 7:144, new design-system component, alert icon, no action) saying "Couldn’t update favourites. Try again." when such a change fails. Needs a one-off event channel in `VerbListViewModel` and an `ObserveAsEvents` helper in `:core:presentation`.
- Not in this change: a Retry action on the snackbar, typed results for other repositories (`TestScoreRepository` and the like; only favourites is converted here), and removing PRISM, renaming the `prism` plugins and rule module, the agent hooks (all in `remove-prism`), and the flaky `AppKoinGraphTest`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `user-data`: favourite changes return a typed result.
- `design-system-components`: Snackbar.
- `verb-list-screen`: message when a favourite change fails.
- `continuous-integration`: the checks and unit tests check also covers ktlint and enforces the per-module coverage floors; the floors and the exempt modules are stated as requirements.

## Impact

- **Requirement:** Notion requirements page, section 14, **N-16 Code quality gates** (added 2026-11-09): https://app.notion.com/p/VerbeClair-requirements-3f1d02620bc1804180a7f241233fdacb
- **Figma:** the Snackbar component, node 7:144 (Components, 3:64): https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-144. No screen frame shows the favourite failure, so its message and icon were decided with the user on 2026-11-09. The rest of the change is tooling only.
- **Code:** `tooling/konsist` (`ProjectScope`, `ScopeIntegrityTest`, rule tests), root `detekt.yml`, root `.editorconfig`, `build-logic` (`KtlintConventionPlugin`, `JacocoConventionPlugin`), `.github/workflows/ci.yml`, and any production or test file that the newly applied rules flag.
- **Developer impact:** `./gradlew build` now also fails on formatting and on coverage below a floor; findings that the wider patterns reveal are fixed in this change or recorded in the module baselines.
- **Notion:** N-17 (typed results) was added to the requirements page on 2026-11-09, and the favourite failure message is in section 6 in L-13 (acceptance criteria included).
- **Spec:** delta to `openspec/specs/continuous-integration/spec.md`, and new requirements in `user-data`, `design-system-components` and `verb-list-screen`.
