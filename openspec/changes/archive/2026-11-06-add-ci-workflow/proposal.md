## Why

VerbeClair has no CI. The definition of done is checked only by hand on a developer machine: `./gradlew build` for
every module, including the iOS targets, plus the Android device tests. Pull requests into `dev` and `main` can
therefore be merged without proof that they build and pass their tests. The requirements also plan GitHub Actions
with repository secrets (A-2, A-9). The first of those secrets, `FRENCH_VERBS_DATASET_TOKEN`, is needed as soon as
`add-verb-list-screen` adds the authenticated dataset download.

## What Changes

- Add a GitHub Actions workflow, `.github/workflows/ci.yml`. It runs on every pull request into `dev` or `main` and
  on every push to `dev`. Drafts and changes that only touch documentation or agent configuration skip the three jobs
  to save billed minutes (added 2026-10-30, after the free Actions allowance ran out).
- **Checks and unit tests job (Linux):** runs `./gradlew build` from the repository root. On Linux the Apple
  targets are skipped, so this covers the Android build, lint, detekt and the JVM / Android host unit tests of every
  module.
- **iOS simulator tests job (macOS):** compiles the iOS targets and runs every module's `commonTest` and `iosTest`
  on the iOS simulator. These are simulator tests (Compose UI tests included), not end-to-end tests of the installed
  app.
- **Android device tests job (Linux, emulator):** runs `./gradlew connectedAndroidDeviceTest` on an Android emulator.
  This runs every module's `commonTest` as instrumented tests. The runner is small (2 cores, about 7 GB), so the test
  APKs are built before the emulator starts and the Gradle and Kotlin daemons are stopped first.
- The three jobs together cover what `./gradlew build` covers on a Mac, plus the device tests.
- All three jobs get the `FRENCH_VERBS_DATASET_TOKEN` repository secret as an environment variable, so the dataset
  download (A-2) works without `local.properties`.
- Gradle, Kotlin/Native and emulator caching keep runs short. A newer push to the same branch or PR cancels the run
  in progress.
- The three job names are stable, so the user can mark them as required status checks in the branch protection of
  `dev` and `main`. The user changes the repository settings; the workflow does not.
- Not in this change: release builds and signing, store uploads, Sentry symbol upload, the API keys and AdMob IDs of
  A-9, and the `prodRelease` "newer dataset available" check (A-2). Those come with the flavors and release work.

## Capabilities

### New Capabilities
- `continuous-integration`: when CI runs, what it checks (the definition of done: checks and unit tests, iOS
  simulator tests and Android device tests), how it gets its secrets, and how its results gate merges into `dev` and `main`.

### Modified Capabilities
- None.

## Requirements and design sources

- Notion: [VerbeClair requirements](https://app.notion.com/p/VerbeClair-requirements-3f1d02620bc1804180a7f241233fdacb):
  - A-2: the dataset token is "kept in `local.properties` on a developer machine and in a CI secret"
  - A-9: "CI: GitHub Actions, with these secrets…"
  - the *Dataset token setup* steps ("once per developer machine and once for CI")
  - the resolved question *Downloading the dataset from a private repository*
- `openspec/config.yaml`: the definition of done and the branching rules (merge into `dev` / `main` only through
  approved pull requests).
- Figma: no screens or components are affected.
- Decided by the user on 2026-10-16 (not specified in Notion):
  - CI runs on pull requests into `dev` / `main` and on pushes to them;
  - CI checks the full definition of done, including the Android emulator tests (split into three jobs on 2026-10-28);
  - the user will make the CI checks required in GitHub branch protection (all three, after the 2026-10-28 split);
  - the repository secret is named `FRENCH_VERBS_DATASET_TOKEN` and holds the same token as `local.properties`.

## Impact

- **New file:** `.github/workflows/ci.yml`. No app code or Gradle build logic changes.
- **Cost:** the repository is private, so Actions minutes count against the account's quota, and macOS minutes count
  10×. Only the iOS job runs on macOS now; the checks and the emulator jobs run on Linux. Caching and
  cancel-in-progress keep this down.
- **Repository settings (user):**
  - add the `FRENCH_VERBS_DATASET_TOKEN` Actions secret
  - add the three checks as required on `dev` and `main`
- **Dependabot:** if it is enabled later, its PRs don't receive Actions secrets, so the dataset download would fail
  there. Noted in design.md.
