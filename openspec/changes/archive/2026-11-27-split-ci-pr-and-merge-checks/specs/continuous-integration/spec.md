## MODIFIED Requirements

### Requirement: When CI runs
CI SHALL run on every pull request whose base branch is `dev` or `main` (when it is opened, updated, reopened or marked
ready for review), on every push to `dev` or `main`, and when started by hand. It SHALL NOT run
on pushes to other branches unless they are part of such a pull request. It has two tiers. The **fast checks** (checks
and unit tests, iOS simulator tests) SHALL run for pull requests into `dev` or `main` and for pushes to `dev`. The
**heavy checks** (Android release build, Android device tests, iOS frameworks) SHALL run for pull requests into `main`,
and pushes to `main`. A push to `main` SHALL also run the iOS simulator tests. A run started by
hand SHALL run every check. When a newer run starts for the same pull request, or for a newer push to `dev`, the older
run SHALL be cancelled. Runs for pushes to `main` SHALL NOT be cancelled once started. CI has no scheduled runs; running
the heavy checks nightly on `dev` is a possible later addition, listed in Notion under "Later versions".

#### Scenario: Pull request into dev
- **WHEN** a pull request from a work branch into `dev` is opened
- **THEN** the fast checks run and the heavy checks are skipped

#### Scenario: Pull request into main
- **WHEN** a pull request from `dev` into `main` is opened
- **THEN** the fast checks and the heavy checks run

#### Scenario: Merge into dev
- **WHEN** a pull request is merged into `dev`
- **THEN** the fast checks run on the resulting `dev` commit

#### Scenario: Merge into main
- **WHEN** `dev` is merged into `main` through a pull request
- **THEN** the heavy checks and the iOS simulator tests run on the resulting `main` commit, and the checks and unit
  tests don't

#### Scenario: New commit on an open pull request
- **WHEN** a newer commit is pushed to an open pull request, or to `dev`, while the older run is in progress
- **THEN** the older run of the same pull request, or of the `dev` push, is cancelled

#### Scenario: Push to dev with an open release pull request
- **WHEN** a commit is pushed to `dev` while a pull request from `dev` into `main` is open
- **THEN** both the `dev` push run and the pull request run happen, and neither cancels the other

#### Scenario: Work branch without a pull request
- **WHEN** a commit is pushed to a work branch that has no open pull request into `dev` or `main`
- **THEN** CI does not run

### Requirement: Runs that prove nothing are skipped
CI SHALL NOT start the check jobs for a draft pull request. For a pull request update into `dev`, or a push to `dev`,
whose files are all documentation (`openspec/`, `docs/`, `*.md`, `LICENSE`) or agent configuration (`.claude/`), CI
SHALL skip them only when the previous head's checks and unit tests and iOS simulator tests passed or were skipped. A
non-draft pull request into `main` and a push to `main` SHALL always run. Any other file, and any case where the changed
files can't be determined, SHALL count as code. A skipped job SHALL report Success, so that required checks do not
block the pull request.

#### Scenario: Documentation-only commit after a passing head
- **WHEN** a commit that only changes files under `openspec/` is pushed to an open pull request into `dev`, and the
  previous head's two fast checks passed or were skipped
- **THEN** the check jobs are skipped, their checks show as passing, and only the change-detection job uses runner time

#### Scenario: Documentation-only commit after a failing head
- **WHEN** a commit that only changes files under `openspec/` is pushed, and either fast check of the previous head
  failed, was cancelled, is still running or is missing
- **THEN** the fast checks run, so documentation can't turn a broken head green

#### Scenario: Documentation-only commit on a pull request into main
- **WHEN** a commit that only changes documentation is pushed to a non-draft pull request into `main`
- **THEN** the fast and heavy checks run

#### Scenario: Workflow or script change
- **WHEN** a commit changes `.github/workflows/ci.yml` or a file under `scripts/`
- **THEN** the jobs of the event's tier run

#### Scenario: Draft pull request
- **WHEN** a commit is pushed to a draft pull request
- **THEN** the check jobs are skipped

#### Scenario: Draft marked ready
- **WHEN** a draft pull request is marked ready for review
- **THEN** CI runs on its head commit

### Requirement: Checks and unit tests check
CI SHALL run, on a Linux runner and as explicit Gradle tasks, the Android debug build, Android lint of the app's debug
build, the JVM and Android host unit tests, compilation of every module's Android instrumented tests without running
them, ktlint formatting, detekt (including its type-resolved analysis of the app's debug build) with the tests of the
project's own rules, and the Konsist architecture rules. It SHALL NOT build the Android release variant. The Konsist
rules SHALL apply to all production source sets (`commonMain`, `androidMain`, `iosMain`, `jvmMain`) and to all test
source sets (`commonTest`, `iosTest`, `androidHostTest`, `androidDeviceTest`), and detekt and ktlint SHALL apply the
test-specific settings to all of the test source sets. The check SHALL fail if any task fails, and SHALL fail when a
test, detekt, ktlint or debug lint task of `./gradlew build` is neither in its task list nor in a named exclusion.

#### Scenario: Everything builds
- **WHEN** the debug build, every unit test and every static check passes
- **THEN** the checks and unit tests check passes

#### Scenario: Unit test failure
- **WHEN** a unit test or a detekt rule fails
- **THEN** the checks and unit tests check fails and its log shows the failure

#### Scenario: Broken instrumented test source
- **WHEN** an Android instrumented test source file no longer compiles
- **THEN** the checks and unit tests check fails without starting an emulator

#### Scenario: New module's tests are missing from the list
- **WHEN** a change adds a module whose test task is not in the task list
- **THEN** the checks and unit tests check fails and its log names the missing task

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
CI SHALL run every module's `commonTest` and `iosTest` tests on the iOS simulator, as a debug test build, on a macOS
runner with Xcode. It SHALL NOT compile for iOS devices or link the frameworks. The check fails if any task fails.

#### Scenario: iOS tests pass
- **WHEN** the iOS simulator code compiles and every test passes on the iOS simulator
- **THEN** the iOS simulator tests check passes

#### Scenario: iOS test failure
- **WHEN** an iOS simulator test fails
- **THEN** the iOS simulator tests check fails and its log shows the failing test, while the other checks are
  unaffected

### Requirement: Android device tests check
CI SHALL run `./gradlew connectedAndroidDeviceTest` on an Android emulator in the heavy tier, so that every module's
`commonTest` (including Compose UI tests) runs as instrumented tests. The check fails if any test fails or the emulator
can't start.

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

#### Scenario: Pull request into dev
- **WHEN** a pull request into `dev` is opened or updated
- **THEN** no emulator is started

### Requirement: Merge gating
Every check job and the change-detection job SHALL have stable names. `dev` SHALL require `Changes`,
`Checks and unit tests` and `iOS simulator tests`. `main` SHALL require those three and `Android release build`,
`Android device tests` and `iOS frameworks`. `Changes` is required because the jobs that depend on it are skipped, and
so count as passed, when it fails. A pull request whose required checks have not passed SHALL NOT be mergeable once the
settings are applied. The settings are applied only with the user's approval.

#### Scenario: Failing check blocks the merge
- **WHEN** the required checks are enabled and any one of them fails on a pull request
- **THEN** GitHub does not allow merging that pull request

#### Scenario: Change detection fails
- **WHEN** the change-detection job fails
- **THEN** the pull request can't be merged

#### Scenario: All checks pass
- **WHEN** all required checks pass and the user approves of the change
- **THEN** the pull request can be merged

## ADDED Requirements

### Requirement: Android release build check
In the heavy tier, CI SHALL build the Android release variant of the app and run detekt's type-resolved analysis of
the release build. The check fails if either fails.

#### Scenario: Release build breaks
- **WHEN** the release variant of a pull request into `main` doesn't build
- **THEN** the `Android release build` check fails and its log shows the failure

### Requirement: iOS frameworks check
In the heavy tier, CI SHALL link the debug and release frameworks for the iOS simulator and the iOS device on a macOS
runner, in a job of its own with a 60 minute limit. The check fails if any framework fails to link or the limit is
reached. CI does not build the Xcode app.

#### Scenario: Frameworks link
- **WHEN** all four frameworks link
- **THEN** the `iOS frameworks` check passes

#### Scenario: Device code doesn't compile
- **WHEN** production code compiles for the simulator but not for an iOS device
- **THEN** the `iOS frameworks` check fails

### Requirement: Mutation testing
The checks and unit tests job SHALL run Pitest mutation testing on the JVM unit tests of `:core:domain` and
`:core:database`. No minimum score SHALL be enforced. An error while running Pitest SHALL fail the job. The score
SHALL be shown in the run's summary, and the report SHALL be available from the run.

#### Scenario: Low mutation score
- **WHEN** Pitest completes and some mutations survive
- **THEN** the job doesn't fail because of the score, and the summary and report show the result

#### Scenario: Pitest can't run
- **WHEN** Pitest stops with an error
- **THEN** the checks and unit tests check fails and its log shows the error
