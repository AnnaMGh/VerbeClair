## 1. Konsist sees the shared code

- [ ] 1.1 Spike: change `ProjectScope` to classify by source-set suffix (`*Main` production, `*Test` test), run `./gradlew :tooling:konsist:test`, and record in the pull request how many rules fail and where; verify by the recorded list matching the run output
- [ ] 1.2 Fix each real violation found in 1.1, or add a narrow allow-list entry with a comment saying why; verify `./gradlew :tooling:konsist:test` passes with no `@Ignore` and no weakened rule
- [ ] 1.3 Add a `ScopeIntegrityTest` assertion that every module with Kotlin under a `src/*Main` directory has at least one production file in scope; verify the test fails when the classification is temporarily reverted to `main` only, then passes again

## 2. detekt and ktlint patterns cover every KMP test source set

- [ ] 2.1 Script the replacement of `test` and `androidTest` patterns in `detekt.yml` with the six-name list (`test`, `androidTest`, `commonTest`, `iosTest`, `androidHostTest`, `androidDeviceTest`); verify with `git diff` that every changed line is a pattern line and `grep` finds no remaining two-name pattern
- [ ] 2.2 Widen the `.editorconfig` test override to the same six names; verify `ktlintCheck` accepts a backticked test name in `commonTest`
- [ ] 2.3 Run `./gradlew detekt` and `./gradlew ktlintCheck`; fix findings the wider patterns reveal, or record them in the module baselines so they show in the diff; verify both tasks pass
- [ ] 2.4 Verify the rules bite: add a temporary banned-library import to a `commonTest` file and a wildcard import to a `commonMain` file, confirm detekt or ktlint report each, then revert

## 3. ktlint runs in `check`

- [ ] 3.1 Make each module's `check` depend on its `ktlintCheck` in the ktlint convention plugin; verify with `./gradlew :core:domain:check --dry-run` that `ktlintCheck` is in the task graph
- [ ] 3.2 Verify a formatting violation fails `./gradlew build` (introduce one, confirm the failure names file, line and rule, then revert)

## 4. Coverage ratchet

- [ ] 4.1 Bring over the report logic from `chore/prism-kmp-support` (`git checkout chore/prism-kmp-support -- build-logic/src/main/kotlin/JacocoConventionPlugin.kt`) and verify `./gradlew :core:domain:jacocoXmlReport` writes `build/reports/jacoco/coverage.xml`
- [ ] 4.2 Add `jacocoCoverageVerification` (line counter, minimum from the module's floor) to the plugin, with the 80 % target as one constant and the exempt modules listed once with a reason each; verify the task exists in `:core:domain` and `:core:database` and not in an exempt module
- [ ] 4.3 Set floors from today's measurement, rounded down: `:core:database` 58, `:core:domain` 44; verify `./gradlew :core:database:jacocoCoverageVerification :core:domain:jacocoCoverageVerification` passes, and fails when a floor is temporarily set above the measured value (then revert)
- [ ] 4.4 Add a `jacocoCoverageVerification` step to the checks and unit tests job in `.github/workflows/ci.yml`, after `./gradlew build`; verify the workflow file is valid and the step is present in the job

## 5. Specs and requirement trace

- [ ] 5.1 Confirm Notion N-16 is present in the requirements page and cited in `proposal.md`; verify by fetching the page and finding the row
- [ ] 5.2 Run `openspec validate fix-kmp-static-analysis --strict`; verify it reports no errors

## 6. Done gate

- [ ] 6.1 From the repository root with `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer`, run `./gradlew build` and `./gradlew jacocoCoverageVerification`; verify both pass for every module
- [ ] 6.2 Run the Android device tests on the VerbeClair emulator, `ANDROID_SERIAL=emulator-5574 ./gradlew connectedAndroidDeviceTest`; verify they pass (note the known flaky `AppKoinGraphTest` if it fails and re-run it alone before concluding)
- [ ] 6.3 Open the pull request into `dev`, wait for the three CI jobs, and verify all pass before asking the user to approve the merge
