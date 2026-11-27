## 1. Requirements

- [x] 1.1 Add N-18, the two N-16 wording edits, the "Resolved questions" line and the "Later versions" nightly entry to Notion, and verify by fetching the page again

## 2. Fast tier on Linux

- [x] 2.1 Add the task list file under `scripts/ci/` (design D2) and verify the listed tasks and `jacocoCoverageVerification` pass locally and that the dry run requests no release task and no iOS compile task
- [x] 2.2 Add the drift guard script and its test, and verify it passes with the full list and fails, naming the task, when one is removed
- [x] 2.3 Verify `compileAndroidDeviceTest` fails when an instrumented test file is temporarily broken, then restore the file and confirm the tree is clean

## 3. Workflow

- [x] 3.1 Update `ci.yml` triggers, concurrency and the per-job conditions (design D1, D4) and verify with `actionlint`
- [x] 3.2 Update the skip logic: `previous-head-green.sh` expects the two fast jobs, PRs and pushes to `main` always run; verify `scripts/ci/changed-code-test.sh` covers these cases and passes
- [x] 3.3 Change the Linux job to the task list plus the drift guard, and verify that no job runs `./gradlew build`
- [x] 3.4 Change `iOS simulator tests` to only `iosSimulatorArm64Test` with a 45 minute limit, memory output and the early `~/.konan` save (design D6); verify the early task fetches the toolchain on its own and the tests pass locally
- [x] 3.5 Add `Android release build` and verify its Gradle tasks pass locally
- [x] 3.6 Add `iOS frameworks` (four link tasks, 60 minutes, restore-only cache) and verify the four tasks link locally
- [x] 3.7 Limit `Android device tests` to the heavy tier with its steps unchanged, and verify with `actionlint` and the diff
- [x] 3.8 Update the comments that assume a private repository, including `cleanup-caches.yml`, and any CI wording in `AGENTS.md` that became wrong; verify by searching for the old claims

## 4. Pitest

- [x] 4.1 Spike on `:core:domain` (design D7) and record the chosen approach, test discovery, runtime and noisy mutations in design.md; stop and ask the user if neither approach works
- [x] 4.2 Wire Pitest for `:core:domain` and `:core:database` with versions in the version catalog, and verify the report lists mutants and that weakening an assertion changes the result (then restore it)
- [x] 4.3 Add the report-only CI step with the score in the job summary and the report always uploaded; verify the summary script locally and with `actionlint`

## 5. Done gate

- [x] 5.1 Done gate: all tests pass, `./gradlew build` passes from the root, and the device tests pass on `emulator-5574`

## After archive: the PR into dev and later (not part of the done gate)

- On the PR into `dev`, check that only `Changes`, `Checks and unit tests` and `iOS simulator tests` run, that Linux runs no iOS compile task, and record cold and warm times and macOS memory in the PR description, which also lists the heavy jobs as untested.
- Apply the required checks with the user's go-ahead (design D8), read the settings back, and update the memory note about branch protection.
- On the first PR from `dev` to `main`, confirm the heavy jobs and check `gh cache list`.
