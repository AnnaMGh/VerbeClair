## Purpose

Automatic checks on GitHub for every change headed to `dev` or `main`. CI runs the project's definition of done (checks
and unit tests for every module, the iOS simulator tests, and the Android device tests) so merges are gated on proof rather
than on manual runs.

## ADDED Requirements

### Requirement: When CI runs
CI SHALL run on every pull request whose base branch is `dev` or `main` (when it is opened, updated, reopened or marked
ready for review), and on every push to `dev`. It SHALL NOT run on pushes to `main` or to other branches unless they are
part of such a pull request. When a newer commit arrives for the same pull request or branch, the run in progress SHALL be cancelled.

#### Scenario: Pull request into dev
- **WHEN** a pull request from `feature/verb-list-screen` into `dev` is opened
- **THEN** CI runs on its head commit

#### Scenario: New commit on an open pull request
- **WHEN** a new commit is pushed to the branch of an open pull request while CI is running
- **THEN** the running CI is cancelled and a new run starts for the new commit

#### Scenario: Merge into dev
- **WHEN** a pull request is merged into `dev`
- **THEN** CI runs on the resulting `dev` commit

#### Scenario: Merge into main
- **WHEN** `dev` is merged into `main` through a pull request
- **THEN** CI does not run again on the resulting `main` commit, because the pull request already built that tree

#### Scenario: Work branch without a pull request
- **WHEN** a commit is pushed to a work branch that has no open pull request into `dev` or `main`
- **THEN** CI does not run

### Requirement: Runs that prove nothing are skipped
CI SHALL NOT start the checks and unit tests, iOS simulator tests and Android device tests jobs for a draft pull request,
or for a change whose files are all documentation (`openspec/`, `docs/`, `*.md`, `LICENSE`) or agent configuration
(`.claude/`). Any other file, and any case where the changed files can't be determined, SHALL count as code. A skipped
job SHALL report Success, so that required checks do not block the pull request.

#### Scenario: Documentation-only commit
- **WHEN** a commit that only changes files under `openspec/` is pushed to an open pull request
- **THEN** the three jobs are skipped, their checks show as passing, and only the change-detection job uses runner time

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
includes the Android build, lint, detekt and the JVM / Android host unit tests. The check fails if any task fails.

#### Scenario: Everything builds
- **WHEN** all modules compile and every unit test and static check passes
- **THEN** the checks and unit tests check passes

#### Scenario: Unit test failure
- **WHEN** a unit test or a detekt rule fails
- **THEN** the checks and unit tests check fails and its log shows the failure

### Requirement: iOS simulator tests check
CI SHALL compile the iOS targets and run every module's `commonTest` and `iosTest` tests on the iOS simulator, on a
macOS runner with Xcode. Together with the checks and unit tests check, this SHALL cover everything that
`./gradlew build` covers on a Mac. The check fails if any task fails.

#### Scenario: iOS tests pass
- **WHEN** the iOS targets compile and every test passes on the iOS simulator
- **THEN** the iOS simulator tests check passes

#### Scenario: iOS test failure
- **WHEN** an iOS simulator test fails
- **THEN** the iOS simulator tests check fails and its log shows the failing test, while the other checks are
  unaffected

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
