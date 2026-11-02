## Context

The repository (`AnnaMGh/VerbeClair`, private, default branch `main`) has no `.github/` directory.
- **Gradle daemon:** pinned to JDK 21 (Azul) in `gradle/gradle-daemon-jvm.properties`, with foojay download URLs.
- **iOS:** builds need full Xcode (`simctl`). Locally this is done with `DEVELOPER_DIR` per command (AGENTS.md).
- **Android:** `commonTest` runs as instrumented tests via `connectedAndroidDeviceTest`.
- **Configuration cache and build cache:** both on (`gradle.properties`).
- **Dataset token:** `add-verb-list-screen` reads it from `local.properties` key `frenchVerbsDataset.githubToken`,
  falling back to the environment variable `FRENCH_VERBS_DATASET_TOKEN`.

Requirements are in `specs/continuous-integration/spec.md`.

## Goals / Non-Goals

**Goals:**
- One workflow file whose three jobs together mirror the definition of done, each with a name that says what failed.
- Short reruns through caching, and no wasted minutes on outdated commits.
- Least privilege: read-only token, secrets only as environment variables, actions pinned to commit SHAs.

**Non-Goals:**
- Release, signing, store upload, Sentry symbols, flavors and their secrets (A-9 beyond the dataset token).
- Changing repository settings (secrets, branch protection). The user does this; the tasks list exact steps.
- Static analysis (ktlint/detekt). Neither is configured in the project yet.

## Decisions

### D1. One workflow, three parallel jobs
`.github/workflows/ci.yml`, `name: CI`:

| Job id | Display name (required check) | Runner | Runs |
|---|---|---|---|
| `checks` | `Checks and unit tests` | `ubuntu-latest` | `./gradlew build --stacktrace` (Apple targets are skipped on Linux) |
| `ios-tests` | `iOS simulator tests` | `macos-15` | `./gradlew` with the iOS tasks listed below, `--stacktrace` |
| `android-device-tests` | `Android device tests` | `ubuntu-latest` | `./gradlew connectedAndroidDeviceTest` on an emulator |

The jobs run in parallel, so the slow emulator job doesn't wait for the others.

Why three: a red check now says what broke (lint and unit tests, iOS, or the emulator). The checks job runs on
Linux, so only the iOS tests use the 10× macOS minutes. Together, `checks` and `ios-tests` cover what `./gradlew build`
runs on a Mac. The `ios-tests` command is `iosSimulatorArm64Test compileKotlinIosArm64 compileTestKotlinIosArm64
linkDebugFrameworkIosSimulatorArm64 linkDebugFrameworkIosArm64 linkReleaseFrameworkIosSimulatorArm64
linkReleaseFrameworkIosArm64 compileIosMainKotlinMetadata compileAppleMainKotlinMetadata compileNativeMainKotlinMetadata
metadataIosMainClasses metadataIosMainProcessResources iosArm64MetadataElements iosSimulatorArm64MetadataElements`.
It was derived by comparing `./gradlew build --dry-run` with `--dry-run` of this command: no iOS, Apple or Native task
in `build` is missing from it (2026-10-28). The simulator tests alone were not enough: the first version missed the
framework link and the Apple / Native metadata tasks. The iOS tests are simulator tests,
including Compose UI tests via `runComposeUiTest`; there is no XCUITest end-to-end suite. GitHub shows checks as
`CI / <display name>`, and those exact strings are what the user marks as required.

Alternative: run every job on macOS. Rejected, because macOS minutes cost 10× in a private repo and macOS runners have no
hardware acceleration for the x86_64 emulator. Linux with KVM is faster and cheaper.

### D2. Triggers and concurrency
```yaml
on:
  pull_request:
    branches: [dev, main]
    types: [opened, synchronize, reopened, ready_for_review]
  push:
    branches: [dev]
concurrency:
  group: ci-${{ github.event.pull_request.number || github.ref }}
  cancel-in-progress: ${{ github.event_name == 'pull_request' }}
```
Runs on pushes to `dev` are not cancelled. Each merge result keeps its own status, which makes it clear which
merge broke the branch. Pushes to `main` do not run: `main` only receives `dev` through a pull request, and that
pull request already built the same tree. `ready_for_review` is listed because it is not a default type: without it,
marking a draft ready would not start a run.

### D3. Toolchain
- **JDK:** `actions/setup-java` with `distribution: zulu`, `java-version: 21`, matching the daemon criteria. Gradle's
  daemon toolchain finds it without downloading another JDK.
- **Gradle:** `gradle/actions/setup-gradle` provides the dependency and build cache. Every run may write
  the cache (`cache-read-only: false`; it must be explicit, because `main`, not `dev`, is the default branch). GitHub
  scopes a pull request's caches to that pull request, so `dev` and `main` never read them, and a re-run of the same
  pull request starts warm. The runs on `dev` still seed the cache that new pull requests start from. The repository
  cache limit is 10 GB, and the three Gradle caches, the AVD snapshot and `~/.konan` must fit; check with `gh cache list`.
- **Kotlin/Native:** `actions/cache` on `~/.konan`, keyed by `gradle/libs.versions.toml` (it holds the Kotlin
  version). Only the `ios-tests` job needs it.
- **Xcode:** in `ios-tests` only, the `macos-15` runner's default Xcode is used, with `DEVELOPER_DIR` set from `xcode-select -p` in a step.
  This keeps the same per-command approach as AGENTS.md and changes nothing global. The Xcode version is printed in
  the log.
- **Android SDK:** preinstalled on both runners (`ANDROID_HOME`). No `local.properties` is needed; AGP reads
  `ANDROID_HOME`.

### D8. Saving minutes
The 2026-10-28 billing showed 436 Linux and 73 macOS minutes in one day: $2.62 and $4.53 before the allowance, so the
macOS job was about 63% of the cost with 14% of the minutes. The free allowance (about 2,000 Linux minutes, applied as
a dollar credit) ran out and later jobs failed to start with a payment error. The workflow therefore avoids paying for
runs that prove nothing:
- **`changes` job.** A cheap Linux job (permissions `contents: read` and `checks: read`) runs
  `scripts/ci/changed-code.sh`, which diffs the commits just pushed (`github.event.before` to the new head, for a push
  to `dev` or a pull request update) and answers `code=true|false`. It must not diff the whole pull request against its
  base: a pull request that contains any code change would then never skip (found on 2026-11-01, run 41286493831).
  A new, reopened or ready-for-review pull request always runs. Only `openspec/`, `.claude/`,
  `docs/`, `*.md` and `LICENSE` count as non-code. `.github/`, `scripts/`, `gradle/` and every Gradle file count as
  code, and so does any doubt (no base, a git error, an empty diff). The three jobs use
  `needs: changes` and `if: needs.changes.outputs.run == 'true'`. A job skipped by its `if` reports Success, so the
  required checks of task 3.1 still pass; `paths-ignore` would leave them pending forever.
- **Safety check.** Skipping on the new commits alone would let a docs-only commit turn green on top of a broken or
  cancelled head. A docs-only change therefore skips only when the previous head's three jobs each concluded success or
  skipped (`scripts/ci/previous-head-green.sh`, reading the check runs of that commit through `gh api`). Missing, running,
  failed or cancelled runs, an API error, and a previous head that is no longer in the history all mean "run". A skipped
  job counts as passed because it was skipped under this same rule, or because it was a draft, and
  `ready_for_review` always runs.
- **Drafts.** `run` is also false when the pull request is a draft. Work on a draft costs the `changes` job only.
- **Timeouts.** 25 minutes for `checks` and `ios-tests` and 40 for `android-device-tests` (the normal run takes 10, 8 and
  23), so a stuck job stops early; a stuck macOS job is billed 10×.
- **Fewer duplicate runs.** No run on pushes to `main` (D2) and warm re-runs of the same pull request (D3).

### D4. Emulator job
`reactivecircus/android-emulator-runner`:
- `api-level: 34`, `target: google_apis`, `arch: x86_64`, `disable-animations: true`, `emulator-boot-timeout: 600`
- KVM enabled with the documented udev rule
- AVD snapshot cached with `actions/cache` (key: API level + target), so later runs boot in seconds

**Memory budget.** The runner of a private repository has 2 cores and about 7 GB RAM (the emulator log confirms 2
cores). The Gradle daemon (`-Xmx6144M`), the Kotlin daemon (`-Xmx3072M`) and the emulator (2560 MB) don't fit
together. The first CI runs ended in `System has crashed` and in a step killed with no error message, which fits
memory pressure but is not proven. The job therefore:
1. builds the test APKs first with `./gradlew packageAndroidDeviceTest`, before the emulator starts;
2. stops the Gradle daemon (`./gradlew --stop`) and the Kotlin compile daemon, and prints `free -m`;
3. runs `connectedAndroidDeviceTest` with `-Dorg.gradle.jvmargs=-Xmx1536M` through `scripts/ci/run-device-tests.sh`, where every compile task is up to date;
4. on failure prints `free -m`, `df -h` and the kernel's out-of-memory lines (`dmesg`), so the cause is visible next time.

**Emulator shutdown.** On the run of 2026-10-28 every test passed, but the emulator did not exit after the action's
`adb emu kill`, and the step stayed open for 40 minutes until the job was cancelled. The script therefore stops the
Gradle daemons, runs `adb emu kill`, waits up to 30 s, and then kills `qemu-system` itself; it keeps the Gradle exit code.
The step also has `timeout-minutes: 30`, so a stuck shutdown fails in minutes rather than at the 60 minute job limit.
The cause of the hung emulator is not known.

**Window focus.** `ListControlsSheetDismissTest.backDismissesTheSheet` sends Back through the real input system and failed
on CI in every run that got that far: all 8 attempts over 20 s left the sheet open, while it passes on a local emulator.
Touch-based UI tests are not affected because Compose injects them directly. A sleeping or locked screen was ruled out
(run of 2026-10-28: awake, no keyguard). The same log showed `Application Not Responding: com.google.android.apps.nexuslauncher`
as the focused window after the tests, so a system dialog in front of the app is the likely reason Back is dropped, and
probably also the `RootViewWithoutFocusException` seen on 2026-10-20. The script sets `hide_error_dialogs` and prints the
focused window before and after the tests. When the dialog appeared relative to the failing test is not known.

**Flaky device tests.** The first runs of the full workflow failed in two different tests: `backDismissesTheSheet` (above)
and, once, `AppKoinGraphTest.verbCatalogReadsTheInstalledBundledDatabase` (`FileNotFoundException … verbs.db.tmp`: the
installer's test directory was gone between writing the temporary file and syncing it). The second did not reproduce
locally: 37 of 37 passes on the VerbeClair emulator, 12 of them under artificial CPU load. The script therefore:
- passes `--continue`, so one failing module no longer stops the others from running;
- waits up to 120 s for the real launcher to take focus (the system reports boot complete while it still shows the
  `FallbackHome` placeholder);
- records `logcat` (main, system, crash) for the whole run to `build/ci-logs/logcat.txt`, which the workflow uploads with
  the test reports when the job fails, so the next failure has the system's side of the story.

**Disk budget.** Building the test APKs of every module first uses enough disk that the emulator refuses to start
(`Your device does not have enough disk space to run avd`), and the emulator action then waits its full 600 s boot
timeout. Found on the first run of the split workflow (2026-10-28): the disk check passed before the build and failed
right after it. The job therefore starts by deleting toolchains the project doesn't use (dotnet, Haskell, Boost, Swift,
CodeQL), and prints `df -h` before and after, and again before the emulator starts.
API 34 is the newest image with a mature, reliable CI history. It is between `minSdk 24` and `targetSdk 37`.
Instrumented test reports (`**/build/reports/androidTests/`) are uploaded as an artifact when the job fails.

### D5. Secrets and permissions
- `permissions: contents: read` at workflow level.
- `env: FRENCH_VERBS_DATASET_TOKEN: ${{ secrets.FRENCH_VERBS_DATASET_TOKEN }}` on the Gradle steps only (four: `checks`, `ios-tests`, and the APK build and test run of `android-device-tests`), not
  workflow-wide. GitHub masks the value in logs.
- Before `add-verb-list-screen` is merged, nothing reads the variable, so CI passes whether the secret is set or not.
  After it is merged, a missing secret fails the build with that change's message naming the token.
- PRs from forks and Dependabot PRs don't receive secrets. The repository is private and Dependabot isn't enabled, so
  no special handling for now (see Risks).

### D6. Pinned actions
Every third-party action is pinned to a full commit SHA with the version tag in a comment (e.g.
`actions/checkout@<sha> # v4.2.2`). This protects the workflow, which handles the dataset token, from a moved or
compromised tag. Updating the pins is a manual task, or Dependabot's `github-actions` ecosystem later.

### D7. Failure output
Gradle runs with `--stacktrace`. Each job uploads its reports with `actions/upload-artifact` when it fails, and keeps them for 7 days:
`checks-test-reports` (`build/reports/`, `build/test-results/`), `ios-test-reports` (`build/reports/tests/`,
`build/test-results/`) and `android-test-reports` (`build/reports/androidTests/`, `build/outputs/androidTest-results/`).

## Risks / Trade-offs

- [macOS minutes cost 10× in a private repo] → Caching, cancel-in-progress on PRs, triggering only on PRs and
  `dev` pushes, and the skips and timeouts of D8. If the quota is still a problem, the user can switch to the "iOS only
  on PRs into main" option discussed on 2026-10-16.
- [Emulator flakiness (boot timeouts)] → Cached AVD snapshot, a 600 s boot timeout, animations off. A failing run
  shows the emulator log in the step output.
- [Emulator or step killed on the 2-core / 7 GB runner, or too little disk to start it] → The memory budget in D4 and the failure-only diagnostics
  step. If it still happens, add a swap file or split the device tests by module into separate jobs.
- [`changes` wrongly says docs-only, so a broken change is never built] → The non-code list is short and explicit, any
  doubt means `code=true`, and `scripts/ci/changed-code-test.sh` covers the decisions. The `dev` push and the pull
  request into `main` still build every code change.
- [`checks` on Linux skips the Apple targets] → `ios-tests` covers exactly those tasks. If a new iOS-only task joins
  `./gradlew build`, add it to the `ios-tests` command.
- [Configuration cache with secrets] → The token is read as an environment variable through a Gradle provider (in
  `add-verb-list-screen`), so it is not stored in the configuration cache entry. Verify there that the CC report shows
  no secret.
- [Dependabot or fork PRs get no secrets] → Not relevant today. If Dependabot is enabled, add a Dependabot secret with
  the same name, or skip the dataset task for `dependabot[bot]`.
- [Runner image changes (default Xcode or SDK updates)] → The Xcode and SDK versions are printed in the log. Pin
  `macos-15` (not `macos-latest`) so the image only changes when it is bumped on purpose.

## Migration Plan

1. Merge this change into `dev` through a PR. The PR itself is the first CI run.
2. The user adds the `FRENCH_VERBS_DATASET_TOKEN` secret (already planned).
3. After one green run on `dev`, the user marks `CI / Checks and unit tests`, `CI / iOS simulator tests` and
   `CI / Android device tests` as required on `dev` and `main`.

Rollback: delete the workflow file (and untick the required checks).
