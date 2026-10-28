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
  push:
    branches: [dev, main]
concurrency:
  group: ci-${{ github.event.pull_request.number || github.ref }}
  cancel-in-progress: ${{ github.event_name == 'pull_request' }}
```
Runs on pushes to `dev`/`main` are not cancelled. Each merge result keeps its own status, which makes it clear which
merge broke a branch.

### D3. Toolchain
- **JDK:** `actions/setup-java` with `distribution: zulu`, `java-version: 21`, matching the daemon criteria. Gradle's
  daemon toolchain finds it without downloading another JDK.
- **Gradle:** `gradle/actions/setup-gradle` provides the dependency and build cache. Writing to the cache is allowed
  only on `dev`/`main` pushes (`cache-read-only: ${{ github.event_name == 'pull_request' }}`), so PRs read the cache
  without polluting it.
- **Kotlin/Native:** `actions/cache` on `~/.konan`, keyed by `gradle/libs.versions.toml` (it holds the Kotlin
  version). Only the `ios-tests` job needs it.
- **Xcode:** in `ios-tests` only, the `macos-15` runner's default Xcode is used, with `DEVELOPER_DIR` set from `xcode-select -p` in a step.
  This keeps the same per-command approach as AGENTS.md and changes nothing global. The Xcode version is printed in
  the log.
- **Android SDK:** preinstalled on both runners (`ANDROID_HOME`). No `local.properties` is needed; AGP reads
  `ANDROID_HOME`.

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
3. runs `connectedAndroidDeviceTest` with `-Dorg.gradle.jvmargs=-Xmx1536M`, where every compile task is up to date;
4. on failure prints `free -m` and the kernel's out-of-memory lines (`dmesg`), so the cause is visible next time.

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

- [macOS minutes cost 10× in a private repo] → Caching, cancel-in-progress on PRs, and triggering only on PRs and
  `dev`/`main` pushes. If the quota becomes a problem, the user can switch to the "iOS only on PRs into main" option
  discussed on 2026-10-16.
- [Emulator flakiness (boot timeouts)] → Cached AVD snapshot, a 600 s boot timeout, animations off. A failing run
  shows the emulator log in the step output.
- [Emulator or step killed on the 2-core / 7 GB runner] → The memory budget in D4 and the failure-only diagnostics
  step. If it still happens, add a swap file or split the device tests by module into separate jobs.
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
