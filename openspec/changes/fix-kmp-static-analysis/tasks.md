## 1. Konsist sees the shared code

- [x] 1.1 Spike: change `ProjectScope` to classify by source-set suffix (`*Main` production, `*Test` test), run `./gradlew :tooling:konsist:test`, and record in the pull request how many rules fail and where; verify by the recorded list matching the run output
- [x] 1.2 Fix each real violation found in 1.1, or add a narrow allow-list entry with a comment saying why; verify `./gradlew :tooling:konsist:test` passes with no `@Ignore` and no weakened rule
- [x] 1.3 Add a `ScopeIntegrityTest` assertion that every module with Kotlin under a `src/*Main` directory has at least one production file in scope; verify the test fails when the classification is temporarily reverted to `main` only, then passes again

## 2. detekt and ktlint patterns cover every KMP test source set

- [x] 2.1 Script the replacement of `test` and `androidTest` patterns in `detekt.yml` with the six-name list (`test`, `androidTest`, `commonTest`, `iosTest`, `androidHostTest`, `androidDeviceTest`); verify with `git diff` that every changed line is a pattern line and `grep` finds no remaining two-name pattern
- [x] 2.2 Widen the `.editorconfig` test override to the same six names; verify `ktlintCheck` accepts a backticked test name in `commonTest`
- [x] 2.3 Run `./gradlew detekt` and `./gradlew ktlintCheck`; fix findings the wider patterns reveal, or record them in the module baselines so they show in the diff; verify both tasks pass
- [x] 2.4 Verify the rules bite: add a temporary banned-library import to a `commonTest` file and a wildcard import to a `commonMain` file, confirm detekt or ktlint report each, then revert

## 3. ktlint runs in `check`

- [x] 3.1 Make each module's `check` depend on its `ktlintCheck` in the ktlint convention plugin; verify with `./gradlew :core:domain:check --dry-run` that `ktlintCheck` is in the task graph
- [x] 3.2 Verify a formatting violation fails `./gradlew build` (introduce one, confirm the failure names file, line and rule, then revert)

## 4. Coverage ratchet

- [x] 4.1 Bring over the report logic from `chore/prism-kmp-support` (`git checkout chore/prism-kmp-support -- build-logic/src/main/kotlin/JacocoConventionPlugin.kt`) and verify `./gradlew :core:domain:jacocoXmlReport` writes `build/reports/jacoco/coverage.xml`
- [x] 4.2 Add `jacocoCoverageVerification` (line counter, minimum from the module's floor) to the plugin, with the 80 % target as one constant and the exempt modules listed once with a reason each; verify the task exists in `:core:domain` and `:core:database` and not in an exempt module
- [x] 4.3 Set floors from today's measurement, rounded down: `:core:database` 58, `:core:domain` 43 (measured 43.4 % after the favourites work added code; 44 was the earlier figure); verify `./gradlew :core:database:jacocoCoverageVerification :core:domain:jacocoCoverageVerification` passes, and fails when a floor is temporarily set above the measured value (then revert)
- [x] 4.4 Add a `jacocoCoverageVerification` step to the checks and unit tests job in `.github/workflows/ci.yml`, after `./gradlew build`; verify the workflow file is valid and the step is present in the job

## 5. Specs and requirement trace

- [x] 5.1 Confirm Notion N-16 is present in the requirements page and cited in `proposal.md`; verify by fetching the page and finding the row
- [x] 5.2 Run `openspec validate fix-kmp-static-analysis --strict`; verify it reports no errors

## 6. Typed results and the favourite failure message

- [x] 6.1 Add N-17 to the Notion requirements page; verify by fetching the page and finding the row
- [x] 6.2 Add `Result`, `EmptyResult`, `DataError` and their helpers to `:core:domain` and return `EmptyResult<DataError.Local>` from `FavouritesRepository.add` and `remove` (the Room implementation catches `SQLiteException`); verify with `RoomFavouritesRepositoryTest`, including a test that drops the table and gets `DataError.Local.UNKNOWN`
- [x] 6.3 Add `VerbeClairSnackbar` (Figma 7:144) with a light and a dark `@Preview`, each preceded by the Figma URL; verify with `./gradlew :core:designsystem:detekt` and a preview render compared with the Figma frame
- [x] 6.4 Emit `VerbListEvent.FavouriteUpdateFailed` from `VerbListViewModel` and show the snackbar in `VerbListRoot` through `ObserveAsEvents`, with the message in the English, French and Romanian strings; verify with `VerbListViewModelTest` (event on failure, none on success) and `VerbListRootTest.aFailedFavouriteChangeShowsTheSnackbar`
- [x] 6.7 Add the detekt rule `PreviewInSameFileAsComposable` with tests, activate it in `detekt.yml`, and merge every `*Previews.kt` into the file it previews; verify `./gradlew build` passes, no `*Previews.kt` is left, and a temporary separate preview file makes `:core:designsystem:detekt` fail
- [x] 6.5 Add the failure message to the Notion requirements next to L-13; verify by fetching the page and finding it
- [x] 6.6 Compare the snackbar on phone portrait, phone landscape and tablet, in light and dark, with Figma; verified by hand in the Android Studio preview pane (`render-compose-preview` fails on `commonMain` previews)

## 7. Done gate

- [ ] 7.1 From the repository root with `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer`, run `./gradlew build` and `./gradlew jacocoCoverageVerification`; verify both pass for every module
- [ ] 7.2 Run the Android device tests on the VerbeClair emulator, `ANDROID_SERIAL=emulator-5574 ./gradlew connectedAndroidDeviceTest`; verify they pass (note the known flaky `AppKoinGraphTest` if it fails and re-run it alone before concluding)
- [ ] 7.3 Open the pull request into `dev`, wait for the three CI jobs, and verify all pass before asking the user to approve the merge
