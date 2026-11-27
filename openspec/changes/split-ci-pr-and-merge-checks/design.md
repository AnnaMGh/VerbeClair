## Context

See proposal.md for motivation. Current state on `dev`: one workflow with `Changes` and three jobs; the Linux job runs
`./gradlew build`. The repository is now public (until the store launch), so standard runners are free; the archived
`add-ci-workflow` design assumed a private repository. The Linux log of run 45628974786 reports about 16 GB of RAM, and
shows iOS klib compilation running there while `link` tasks are skipped. At the time of exploring, no macOS cache
existed and about 7.8 of 10 GB of cache was used. No Figma screens, components, theme tokens or layouts are involved.

## Goals / Non-Goals

**Goals:** a fast tier for everyday work; real checks before `main`; no module silently dropped from CI; a first
measure of unit-test quality.

**Non-Goals:** building the `iosApp` Xcode project; tests compiled in release mode; signing or store uploads; a
mutation score floor; changing local gates; anything from PR #20 (release-link watchdog and its gate logic).

## Decisions

### D1. Tiers by event
| Job (required name) | PR to dev | push dev | PR to main | push main |
|---|---|---|---|---|
| `Checks and unit tests` | x | x | x | |
| `iOS simulator tests` | x | x | x | x |
| `Android release build` | | | x | x |
| `Android device tests` | | | x | x |
| `iOS frameworks` | | | x | x |

A manual run (`workflow_dispatch`) runs all five. Heavy jobs are separate: they fail and re-run independently, the
release build's memory doesn't compete with the emulator, and a slow link can't delay or hide test results. Cost:
repeated setup and compilation.

### D2. Explicit Linux task list, kept in one file under `scripts/ci/`
`:androidApp:assembleDebug :androidApp:lintDebug :androidApp:testDebugUnitTest :shared:testAndroidHostTest
:core:domain:jvmTest :core:database:jvmTest :tooling:detekt-rules:test :tooling:konsist:test detekt
:androidApp:detektDebug ktlintCheck compileAndroidDeviceTest`, then `jacocoCoverageVerification`.
`:androidApp:detektMain` is `detektDebug` + `detektRelease`; the release half moves to the release job.
`:androidApp:compileDebugAndroidTestKotlin` and `assembleDebugAndroidTest` were rejected: `:androidApp` has no
instrumented tests, while the unqualified `compileAndroidDeviceTest` covers the 7 KMP modules.
**Drift guard:** a script compares `./gradlew build --dry-run` with the list and fails on a missing test, detekt,
ktlint or debug lint task. Exclusions are named (aggregates such as `:androidApp:test` and `:androidApp:detektMain`,
release variants, iOS). It also requires `compileAndroidDeviceTest`, which `build` doesn't include.
It requires `:androidApp:assembleDebug` too: it isn't a check task, so removing it would otherwise go unnoticed.
Alternative: keep `./gradlew build`. Rejected on the mentor's advice (it also builds and checks release).

### D3. No iOS compile tasks on Linux
Chosen by the user. Device (`iosArm64`) compile errors in production code appear in `iOS frameworks`; device test code
is no longer compiled in CI. The standalone Apple / Native metadata tasks are no longer requested.

### D4. Concurrency
Group `${{ github.workflow }}-${{ github.event_name }}-${{ github.ref }}` (the event name keeps a manual run apart
from a push run on the same branch); `cancel-in-progress` for pull requests and
pushes to `dev` only. A `dev` push and the `dev` to `main` pull request have different refs, so both run. Grouping by
branch name would merge them, but could cancel required checks or the run that seeds `dev` caches.

### D5. Skip logic
Docs-only skipping stays for `dev` only; `previous-head-green.sh` expects the two fast jobs. A non-draft PR to `main`
and a push to `main` always run, so a skipped heavy job can't hide an earlier failure.
The rule lives in `scripts/ci/decide-jobs.sh`, so that `changed-code-test.sh` covers it. For each job,
`previous-head-green.sh` takes the newest run that wasn't skipped: a draft pull request into `main` adds skipped runs to
the head of `dev`, which would otherwise hide a failed `dev` run.

### D6. macOS timeout and cache
In a cold run of the larger task list, the last test task started about 20.5 minutes into the Gradle step; the cold
time of `iosSimulatorArm64Test` alone is unmeasured. The timeout goes from 25 to 45 minutes. `~/.konan` is only saved
on success today, so the toolchain is fetched in an early step and saved with `actions/cache/save` before the tests,
provided a light task can fetch it alone and an incomplete download is never saved. The job prints the runner's memory:
the Gradle and Kotlin daemon limits (6 GB and 3 GB) may exceed it; this isn't measured. `iOS frameworks` restores the
cache only and has 60 minutes.

Checked locally on 2026-11-27 with an empty `konan.data.dir`: `:core:domain:downloadKotlinNativeDistribution` fetched
the compiler, LLVM and libffi (1.6 GB). All `iosSimulatorArm64Test` tasks then passed without any further download; they
only added Kotlin/Native's generated caches (`klib/cache`, about 0.5 GB) and a lock file. So the early save is complete,
but it never includes those caches: each run rebuilds them. A save after a restore through `restore-keys` also carries
the older compiler versions that were restored. `iOS frameworks` doesn't write the Gradle cache either.

### D7. Pitest
Starts with a spike on `:core:domain`: `info.solidsoft.pitest` first (mentor's suggestion); its support for a KMP
`jvm()` target is unconfirmed. Fallback: a build-logic convention plugin running the PIT command line through
`JavaExec`, like `KtlintConventionPlugin`. Scope is `:core:domain` and `:core:database`; `:shared` has only a
placeholder host test, and other modules have no JVM or host tests. `:core:database` needs the
`bundledVerbDatabaseDir` property and the built database. If neither approach works, stop and ask the user.

**Spike result (`:core:domain`, 2026-11-27): the command line fallback.**
`info.solidsoft.pitest` 1.19.0 was rejected. Without a Java plugin, and also with `java-base`, it doesn't register its
extension (`Unresolved reference 'pitestVersion'`; `Extension of type 'PitestPluginExtension' does not exist`). With
`java`, the Kotlin plugin fails the build: `'java' Gradle plugin is not compatible with
'org.jetbrains.kotlin.multiplatform' plugin`. `JavaExec` running `pitest-command-line` 1.30.2 works with the
configuration cache, as long as the task keeps only file collections and not Kotlin compilation objects.

- **Test discovery:** the tests run on JUnit 4 (`kotlin-test-junit`), which needs no extra Pitest plugin. Pitest
  discovers 18 tests, the same number `jvmTest` runs. Its "23 tests examined" is the number of classes matching the
  test pattern.
- **Runtime:** about 1 s of Pitest and 4 s of Gradle wall time with the classes already compiled, on the local machine.
- **Result:** 47 mutants without filters. `--avoidCallsTo=kotlin.jvm.internal` removes 3 mutants of compiler-added
  null checks, leaving 44: 23 killed, 2 survived, 19 without coverage.
- **Noise that stays,** because filtering it would also hide real code:
  - both survivors and 1 uncovered mutant come from the inlined stdlib `count` in `lastTestScore` (their line numbers
    are past the end of the file);
  - the 4 mutants in `map`, `onSuccess` and `onFailure` of `Result` show no coverage although `ResultTest` calls them:
    the functions are `inline`, so the tests run copies of the code;
  - uncovered generated getters of `VerbListItem`, `ListFilters` and `ListQuery`. The same list also has real gaps,
    such as `ListFilters.count`, which no `:core:domain` test calls.

**Wiring:** the `verbeclair.pitest` convention plugin adds a `pitest` task to the two modules. It isn't part of `check`
or `build`, so local gates don't change. Classes generated by Room and kotlinx.serialization are excluded.
`:core:database` also passes `bundledVerbDatabaseDir` through `--jvmArgs` and depends on `buildBundledVerbDatabase`.
Its local result: 21 tests discovered (as `jvmTest`), about 12 s of Pitest and 26 s of Gradle wall time with the
classes compiled; 213 mutants, 120 killed, 2 survived, 91 without coverage. Uncovered mutants there include the
database builder tool in `jvmMain` (its tests read the built database instead of calling it) and the user database's
classes, which no JVM test calls.

### D8. Required checks
Applied after the workflow is on `dev` and before the first PR into `main`: read the current settings, show the exact
change, apply only on the user's go-ahead. No required approving reviews (a solo author can't approve their own PR).
Ask whether admins may bypass. Availability on this repository's plan is not yet confirmed.

## Later (nice to have)

A nightly run of the heavy checks on `dev`, so failures show before the PR into `main`. Idea: a `schedule` trigger;
`Changes` checks out `dev`, runs only if its head commit is under 24 hours old, and passes that SHA to the heavy jobs.
To check first: GitHub reads the schedule only from the workflow on `main`, and may attach the results to the latest
`main` commit (not verified), which would clash with N-18's rule on releasing from `main`. A failed night wouldn't be
retried while `dev` is unchanged, and GitHub can delay schedules or disable them in an inactive public repository.

## Risks / Trade-offs

- [Heavy failures can sit on `dev` until the PR into `main`] → accepted; that PR runs them before the merge. See
  "Later".
- [A cancelled `dev` run may not save caches] → the next completed run saves them.
- [Cache limit: `main` can't read `dev`'s caches, and adds its own, including an emulator image of about 2.4 GB;
  unused caches expire after 7 days] → keep the cleanup workflow; check `gh cache list` after the first runs and ask
  before deleting any cache.
- [A cold macOS run may exceed 45 minutes] → D7; measure on the PR.
- [Duplicate run while a release PR is open] → accepted; standard runners are free for public repositories.
- [A skipped heavy job on a PR into `dev` shows success and proves nothing] → documented in N-18.
- [Fork pull requests get no dataset token, so builds needing it fail] → recorded only.
- [Heavy jobs can't be proven by this change's own PR] → report them as untested until the first PR into `main`.

## Migration Plan

1. N-18 is in Notion; implement.
2. Merge into `dev` through a PR; only the fast tier runs there.
3. Apply the required checks (D8).
4. The first PR from `dev` to `main` proves the heavy jobs.
Rollback: revert the workflow change and remove the required check names that no longer exist.
