## MODIFIED Requirements

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

## ADDED Requirements

### Requirement: Coverage floors
The checks and unit tests job SHALL verify the line coverage of the unit tests of every module that has a JVM or Android
host test target against that module's floor, and SHALL fail if any module is below its floor. A floor starts at the
coverage measured when it is introduced and SHALL only be raised, never lowered, unless the pull request states the
reason. The target for every module is 80 %. A module that has no JVM or Android host test target is exempt, and the
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
