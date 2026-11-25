# continuous-integration Specification

## Purpose

Automatic checks on GitHub for every change headed to `dev` or `main`. CI runs the project's definition of done (checks
and unit tests for every module, the iOS simulator tests, and the Android device tests) so merges are gated on proof rather
than on manual runs.

## Requirements

### Requirement: When CI runs
CI SHALL run on every pull request whose base branch is `dev` or `main` (when it is opened, updated, reopened or marked
ready for review), and on every push to `dev`. It SHALL NOT run on pushes to `main` or to other branches unless they are
part of such a pull request. When a newer commit arrives for the same pull request, the run in progress SHALL be cancelled.
A run on a push to `dev` that has started SHALL NOT be cancelled by a newer push, so that the merge result being built
keeps its own status. A `dev` run still waiting behind it MAY be replaced by a newer push.

#### Scenario: Pull request into dev
- **WHEN** a pull request from `feature/verb-list-screen` into `dev` is opened
- **THEN** CI runs on its head commit

#### Scenario: New commit on an open pull request
- **WHEN** a new commit is pushed to the branch of an open pull request while CI is running
- **THEN** the running CI is cancelled and a new run starts for the new commit

#### Scenario: Merge into dev
- **WHEN** a pull request is merged into `dev`
- **THEN** CI runs on the resulting `dev` commit, unless a newer merge replaces a run that is still waiting for the one in progress

#### Scenario: Merge into main
- **WHEN** `dev` is merged into `main` through a pull request
- **THEN** CI does not run again on the resulting `main` commit, because the pull request already built that tree

#### Scenario: Work branch without a pull request
- **WHEN** a commit is pushed to a work branch that has no open pull request into `dev` or `main`
- **THEN** CI does not run

### Requirement: Runs that prove nothing are skipped
CI SHALL NOT start the checks and unit tests, iOS simulator tests and Android device tests jobs for a draft pull request,
or for a change whose files are all documentation (`openspec/`, `docs/`, `*.md`, `LICENSE`) or agent configuration
(`.claude/`). Any other file, and any case where the changed files can't be determined, SHALL count as code. On a push, the previous head's iOS release link job SHALL count as one of the jobs that must have passed. A skipped
job SHALL report Success, so that required checks do not block the pull request.

#### Scenario: Documentation-only commit after a passing head
- **WHEN** a commit that only changes files under `openspec/` is pushed to an open pull request, and the previous head's
  three jobs passed or were skipped, even though the pull request as a whole changes code
- **THEN** the three jobs are skipped, their checks show as passing, and only the change-detection job uses runner time

#### Scenario: Documentation-only commit after a failing head
- **WHEN** a commit that only changes files under `openspec/` is pushed, and any of the previous head's three jobs failed,
  was cancelled, is still running or is missing
- **THEN** the three jobs run, so documentation can't turn a broken head green

#### Scenario: Documentation-only push to dev after a failing release link
- **WHEN** a commit that only changes files under `openspec/` is pushed to `dev`, and the previous head's iOS release
  link job failed, was cancelled, is still running or is missing
- **THEN** the jobs run, including the iOS release link, so documentation can't hide a broken release link on `dev`

#### Scenario: Workflow or script change
- **WHEN** a commit changes `.github/workflows/ci.yml` or a file under `scripts/`
- **THEN** the three jobs run

#### Scenario: Draft pull request
- **WHEN** a commit is pushed to a draft pull request
- **THEN** the three jobs are skipped

#### Scenario: Draft marked ready
- **WHEN** a draft pull request is marked ready for review
- **THEN** CI runs on its head commit

### Requirement: Checks and unit tests check
CI SHALL run `./gradlew build` from the repository root on a Linux runner, covering every Gradle module. This
includes the Android build, lint, ktlint formatting, detekt, the Konsist architecture rules and the JVM / Android
host unit tests. The Konsist rules SHALL apply to all production source sets (`commonMain`, `androidMain`,
`iosMain`, `jvmMain`) and to all test source sets (`commonTest`, `iosTest`, `androidHostTest`,
`androidDeviceTest`), and detekt and ktlint SHALL apply the test-specific settings to all of the test source sets.
The check fails if any task fails.

#### Scenario: Everything builds
- **WHEN** all modules compile and every unit test and static check passes
- **THEN** the checks and unit tests check passes

#### Scenario: Unit test failure
- **WHEN** a unit test or a detekt rule fails
- **THEN** the checks and unit tests check fails and its log shows the failure

#### Scenario: Formatting violation
- **WHEN** a Kotlin file in any source set, `commonTest` included, breaks the formatting rules
- **THEN** the checks and unit tests check fails and its log names the file, line and rule

#### Scenario: Architecture rule violation in shared code
- **WHEN** a class in `commonMain` breaks an architecture rule, for example a ViewModel without a public state flow
- **THEN** the checks and unit tests check fails and its log names the class and the rule

#### Scenario: Preview in a separate file
- **WHEN** a `@Preview` is declared in a file that does not declare the composable it previews, for example a `*Previews.kt` file
- **THEN** the checks and unit tests check fails and its log names the preview

#### Scenario: Test-only rule in a multiplatform test source set
- **WHEN** a test in `commonTest` breaks a rule meant for test code, for example a banned mocking library
- **THEN** the checks and unit tests check fails, in the same way as for a test in `androidHostTest`

### Requirement: iOS simulator tests check
CI SHALL compile the iOS targets and run every module's `commonTest` and `iosTest` tests on the iOS simulator, on a
macOS runner with Xcode. Together with the checks and unit tests check and the iOS release link check, this SHALL
cover everything that `./gradlew build` covers on a Mac. The release frameworks are the one part it does not build,
because they are covered by the iOS release link check. The check fails if any task fails.

#### Scenario: iOS tests pass
- **WHEN** the iOS targets compile and every test passes on the iOS simulator
- **THEN** the iOS simulator tests check passes

#### Scenario: iOS test failure
- **WHEN** an iOS simulator test fails
- **THEN** the iOS simulator tests check fails and its log shows the failing test, while the other checks are
  unaffected

### Requirement: iOS release link check
CI SHALL link the release frameworks for the iOS simulator and the iOS device (`linkReleaseFrameworkIosSimulatorArm64`
and `linkReleaseFrameworkIosArm64`) on a macOS runner, in a job of its own, on every push to `dev` that changes code. It
SHALL NOT run for pull requests, because Kotlin/Native optimizes the whole program for a release framework on a single
thread and the time varies from about 4 minutes to over 30 on the shared runner. The job SHALL have its own timeout of
60 minutes, and a link that stalls SHALL fail the job with the busy Kotlin frames of the Gradle daemon in its log.

#### Scenario: Release frameworks link
- **WHEN** code is pushed to `dev` and both release frameworks link
- **THEN** the iOS release link check passes

#### Scenario: Release link fails or stalls
- **WHEN** a release framework fails to link, or the link runs past the step timeout
- **THEN** the iOS release link check fails on `dev`, where it is visible before the pull request into `main` is approved

#### Scenario: Pull request
- **WHEN** a pull request is opened or updated
- **THEN** the iOS release link job does not run, so a slow release link never holds up the pull request

### Requirement: Android device tests check
CI SHALL run `./gradlew connectedAndroidDeviceTest` on an Android emulator, so that every module's `commonTest`
(including Compose UI tests) runs as instrumented tests. The check fails if any test fails or the emulator can't start.

#### Scenario: Instrumented tests pass
- **WHEN** every instrumented test passes on the emulator
- **THEN** the Android device tests check passes

#### Scenario: Compose UI test failure
- **WHEN** a Compose UI test in `commonTest` fails on the emulator
- **THEN** the Android device tests check fails and the test report is available from the run

#### Scenario: Emulator or runner failure
- **WHEN** the emulator crashes or the test step fails or is killed before the tests finish, while the runner is still alive
- **THEN** the Android device tests check fails, and the run shows the memory state and any out-of-memory kernel
  messages

### Requirement: Pull request caches are removed
The caches written by a pull request SHALL be deleted when the pull request is closed or merged, so that they don't
push the repository over its cache limit and evict the caches of `dev`.

#### Scenario: Pull request merged
- **WHEN** a pull request is merged or closed
- **THEN** every cache under its `refs/pull/<n>/merge` ref is deleted and the caches of `dev` are untouched

#### Scenario: Pull request without caches
- **WHEN** a pull request closes and wrote no caches (for example a draft or a documentation-only one)
- **THEN** the cleanup finishes successfully without deleting anything

### Requirement: Secrets
CI SHALL give the dataset token to Gradle only through the `FRENCH_VERBS_DATASET_TOKEN` repository secret, exposed as
an environment variable, and SHALL NOT need a `local.properties` file. Secret values SHALL never be printed, written to
artifacts, or committed. The workflow SHALL have read-only access to the repository contents.

#### Scenario: Dataset download in CI
- **WHEN** the build needs the bundled dataset
- **THEN** it authenticates with the secret's value and the log shows the value masked

#### Scenario: Secret missing
- **WHEN** the repository secret is not set
- **THEN** the dataset download fails with the build's message naming the missing token, and the check fails

### Requirement: Merge gating
The checks and unit tests check, the iOS simulator tests check and the Android device tests check SHALL have stable names, so they can be required in the branch
protection of `dev` and `main`. A pull request whose required checks have not passed SHALL NOT be mergeable once
the user has enabled them.

#### Scenario: Failing check blocks the merge
- **WHEN** the checks are required on `dev` and any one of them fails on a pull request
- **THEN** GitHub does not allow merging that pull request

#### Scenario: All checks pass
- **WHEN** all three required checks pass and the user approves the pull request
- **THEN** the pull request can be merged

### Requirement: Coverage floors
The checks and unit tests job SHALL verify the line coverage of the unit tests of every module that has a JVM or Android
host test target against that module's floor, and SHALL fail if any module is below its floor. A floor starts at the
coverage measured when it is introduced and SHALL only be raised, never lowered, unless the pull request states the
reason. The target for every module is 80 %. A module that has no JVM or Android host test target is exempt, as are the `:tooling:*` build-tooling
modules (plain Kotlin/JVM, outside the app and removed by the next change), and the
exempt modules SHALL be listed where the floors are defined, so that an exemption is visible and not accidental.

#### Scenario: Module at or above its floor
- **WHEN** a module's measured coverage is equal to or higher than its floor
- **THEN** the coverage check passes for that module

#### Scenario: Module below its floor
- **WHEN** a change lowers a module's measured coverage below its floor
- **THEN** the checks and unit tests check fails and its log names the module, its coverage and its floor

#### Scenario: Floor raised with new tests
- **WHEN** a pull request adds tests that raise a module's coverage and raises its floor to match
- **THEN** the coverage check passes, and later changes are held to the higher floor

#### Scenario: Exempt module
- **WHEN** a module has no JVM or Android host test target
- **THEN** it is listed as exempt, has no floor, and its coverage does not fail the check
