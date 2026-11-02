**Prerequisite (user):** in GitHub → VerbeClair → Settings → Secrets and variables → Actions, add the repository secret `FRENCH_VERBS_DATASET_TOKEN` with the same token as `frenchVerbsDataset.githubToken` in `local.properties`.

## 1. Workflow

- [x] 1.1 Create `.github/workflows/ci.yml` with `name: CI`, the triggers and concurrency of design D2, and `permissions: contents: read`. Verify: `actionlint` (or the GitHub workflow editor) reports no errors.
- [x] 1.2 Add the `checks` job (D1, D3, D5, D7), display name `Checks and unit tests`: `ubuntu-latest`, checkout, setup-java zulu 21, setup-gradle (read-only cache on PRs), `./gradlew build --stacktrace` with `FRENCH_VERBS_DATASET_TOKEN` from secrets, and test reports uploaded on failure. Verify: the job runs green on the PR for this change.
- [x] 1.3 Add the `ios-tests` job (D1, D3, D5, D7), display name `iOS simulator tests`: `macos-15`, checkout, setup-java zulu 21, setup-gradle, `~/.konan` cache, a step exporting `DEVELOPER_DIR` from `xcode-select -p` and printing the Xcode version, the iOS tasks of design D1 (simulator tests, device compilation, framework links, Apple / Native metadata) with the secret, and test reports uploaded on failure. Verify: the job runs green on the PR, and `./gradlew build --dry-run` lists no iOS, Apple or Native task that the command's `--dry-run` lacks (done locally on 2026-10-28: none missing).
- [x] 1.4 Add the `android-device-tests` job (D1, D4, D5, D7), display name `Android device tests`: `ubuntu-latest`, checkout, setup-java zulu 21, setup-gradle, KVM udev rule, AVD snapshot cache, a first step freeing disk space (the emulator needs a free-disk threshold), then `./gradlew packageAndroidDeviceTest` (test APKs built before the emulator), a step stopping the Gradle and Kotlin daemons and printing `free -m`, `df -h` and the largest processes, `reactivecircus/android-emulator-runner` (API 34, google_apis, x86_64, animations off, 600 s boot timeout, 30 min step timeout) running `scripts/ci/run-device-tests.sh` (waits for the launcher, records logcat, runs `./gradlew connectedAndroidDeviceTest -Dorg.gradle.jvmargs=-Xmx1536M --continue --stacktrace`, then stops Gradle and the emulator itself), a failure-only diagnostics step (`free -m`, `df -h`, `dmesg` out-of-memory lines), and the `androidTests` reports uploaded on failure. Verify: the job runs green on the PR and the log lists the instrumented tests of `:core:designsystem` and `:shared`; if it fails, the diagnostics step shows the memory state.
- [x] 1.5 Pin every action to a full commit SHA with its version tag in a comment (D6). Verify: `grep -E 'uses: .+@[0-9a-f]{40}' .github/workflows/ci.yml` matches every `uses:` line.

## 2. Behaviour checks

- [x] 2.1 Check cancel-in-progress. Verify: pushing a second commit to the PR while CI runs cancels the first run, and the new commit gets a fresh run. Checked 2026-10-28: run 40749912380 (`bc34661`) was cancelled, all three jobs, by the push of `317b3ad`, which got run 40750110150.
- [x] 2.2 Check that a failing test fails the right check. Verify: on a throwaway commit, a failing `commonTest` assertion in `:core:designsystem` turns the `Android device tests` and `iOS simulator tests` checks red and their reports artifacts (`android-test-reports`, `ios-test-reports`) are uploaded. `Checks and unit tests` stays green: `:core:designsystem` has no host test (only `withDeviceTestBuilder`) and `iosSimulatorArm64Test` is skipped on `ubuntu-latest`. Then drop the commit (never merged). Checked 2026-10-28 on run 40750110150 (`317b3ad`), commit dropped.
- [x] 2.3 Check that secrets are not exposed. Verify: the workflow logs contain no token value (only `***` where the variable is printed), and no step writes `local.properties`.
- [ ] 2.4 Check the second run time. Verify: on a re-run, the Gradle cache, `~/.konan` and the AVD snapshot are restored. Record the run times of the three jobs in the PR description. Status: `~/.konan` and the AVD snapshot are restored (checked 2026-10-28) and the cold-cache times are in the PR description; the Gradle cache was read-only on PRs; since D8 (section 5) PRs write it, so it is checked on the first re-run of the PR after that change, and the `dev` cache after the first `dev` run.

## 3. Repository settings (user, after merge)

- [ ] 3.1 After the first green run on `dev`, mark `CI / Checks and unit tests`, `CI / iOS simulator tests` and `CI / Android device tests` as required status checks in the branch protection of `dev` and `main`. Verify: a test PR shows all three as "Required", and the merge button stays disabled until they pass.

## 5. Saving minutes (added 2026-10-30, design D2, D3, D8)

- [x] 5.1 Write `scripts/ci/changed-code.sh` and `scripts/ci/changed-code-test.sh`. Verify: `bash scripts/ci/changed-code-test.sh` passes (docs-only, mixed, workflow, empty diff and bad base cases).
- [x] 5.2 Edit `.github/workflows/ci.yml`: the `changes` job, `needs`/`if` on the three jobs, `ready_for_review` trigger, push to `dev` only, timeouts 25/25/40, `cache-read-only: false`. Verify: `actionlint` reports no errors and every `uses:` is still pinned to a SHA.
- [x] 5.3 Check a code change runs everything. Verify: the first push with these changes (it touches `ci.yml`) runs the `changes` job with `run=true` and all three jobs go green. Checked 2026-11-01 on run 41286493831 (`5191bf4`): green. That version diffed the whole PR against `dev`, so it could never skip (see 5.7).
- [ ] 5.4 Check a documentation-only commit is skipped. Verify: a commit touching only `openspec/`, pushed on top of a head whose three jobs passed, leaves the three jobs skipped (shown as passing) and the run costs about one minute; the log shows `previous head ... green: true`.
- [ ] 5.5 Check drafts. Verify: with the PR converted to draft, a push skips the three jobs; marking it ready starts a full run.
- [ ] 5.6 Check the cache fits. Verify: after a re-run of the PR, `gh cache list` shows the Gradle caches of the PR, the AVD snapshot and `~/.konan` together under 10 GB, and the re-run restores the Gradle cache.

- [x] 5.7 Change the gate to look at the pushed commits and to require a green previous head (design D8, safety check): add `scripts/ci/previous-head-green.sh`, its cases in `scripts/ci/changed-code-test.sh`, and the `changes` job steps. Verify: `bash scripts/ci/changed-code-test.sh` passes, `actionlint` is clean, and the script answers `true` for the check runs of `bc34661`, `5191bf4` and `f7dc0e5` and `false` for `7df130f` and `317b3ad` (done locally 2026-11-02).
- [ ] 5.8 Check a docs-only commit after a failing head runs everything. Verify: when the previous head's run was cancelled or failed, the next `openspec/`-only commit runs all three jobs and the log shows `previous head ... green: false`.

## 4. Done gate

- [x] 4.1 Run `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew build` and `./gradlew connectedAndroidDeviceTest` locally (no app code changes, so this confirms nothing regressed). Verify: both succeed locally and all three CI jobs are green on the PR into `dev`, with the outputs recorded.
