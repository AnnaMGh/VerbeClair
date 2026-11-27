## Why

Mentor feedback: every pull request runs more than it needs to. The iOS job on `dev` also times out at 25 minutes
and leaves no macOS cache for later runs. CI needs a fast tier for everyday work and a heavy tier before and after
code reaches `main`, plus a check of how good the unit tests are.

## What Changes

- **Fast checks** (pull requests into `dev` or `main`, and pushes to `dev`): explicit Gradle tasks on Linux
  (Android debug build, app lint, JVM / Android host unit tests, ktlint, detekt and its rule tests, Konsist,
  compilation of the instrumented tests, coverage floors, Pitest) and only `iosSimulatorArm64Test` on macOS.
  **BREAKING:** CI no longer runs `./gradlew build`.
- **Heavy checks** (pull requests into `main`, pushes to `main`): Android release build, Android device tests, iOS
  debug and release frameworks. A push to `main` also runs the iOS simulator tests.
- **BREAKING:** pull requests into `dev` and pushes to `dev` no longer run the Android device tests or compile for
  iOS devices.
- **Pitest** mutation testing of the JVM unit tests, report only.
- Concurrency cancels older runs of the same pull request or of `dev`.
- A drift guard fails CI when a verification task of the full build is missing from the explicit task list.
- Required checks on `dev` and `main` (repository settings, applied with the user's go-ahead).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `continuous-integration`: when CI runs, what each tier checks, skip rules, merge gating and mutation
  testing.

## Requirements and design sources

- Notion: [VerbeClair requirements](https://app.notion.com/p/VerbeClair-requirements-3f1d02620bc1804180a7f241233fdacb):
  N-16 (code quality gates), A-2 and A-9 (CI secrets).
  N-18 (continuous integration, added on 2026-11-26), and "Later versions" (nightly CI run).
- Decided by the user on 2026-11-26: the two tiers and their contents, compiling the instrumented tests in the fast
  tier, Pitest report only, one change, the required checks and concurrency. A nightly run of the heavy checks was
  planned and then left for later.
- Figma: no screens or components are affected.

## Later (nice to have, not in this change)

- A nightly run of the heavy checks on `dev` (see design.md, "Later").

## Impact

- `.github/workflows/ci.yml`, the comment in `.github/workflows/cleanup-caches.yml`, `scripts/ci/`.
- `build-logic/`, `gradle/libs.versions.toml`, `core/domain` and `core/database` build files (Pitest).
- CI wording in `AGENTS.md` that becomes wrong. Local gates (`./gradlew build`, hooks, `/finalize`) don't change.
- Repository settings: branch protection of `dev` and `main`.
- PR #20 is not part of this change.
