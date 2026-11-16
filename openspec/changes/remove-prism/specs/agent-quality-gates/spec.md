## Purpose

Defines the automatic checks that run on the AI agent's own actions and the developer-only `/finalize` that the agent
needs before it opens a pull request, so agent work meets the code quality gates (Notion N-16) without ever blocking
the developer's own git and `gh` commands.

## ADDED Requirements

### Requirement: Format a Kotlin file after the agent edits it
After the agent edits or writes a Kotlin source or Gradle Kotlin script, the project SHALL format that one file with
ktlint and SHALL report to the agent any violation that formatting cannot fix. Other files SHALL be ignored.

#### Scenario: Kotlin file with a formatting violation
- **WHEN** the agent writes a `.kt` file with wrong indentation
- **THEN** the file is reformatted in place and the agent is not interrupted

#### Scenario: Violation that formatting cannot fix
- **WHEN** the agent writes a `.kt` file with a wildcard import
- **THEN** the agent is told the violation and the file it is in

#### Scenario: Non-Kotlin file
- **WHEN** the agent edits a Markdown or JSON file
- **THEN** no formatting check runs

#### Scenario: Formatter cannot run
- **WHEN** the ktlint tooling is not available
- **THEN** the edit is allowed and no error is raised

### Requirement: Check the agent's work when it stops
When the agent stops after changing Kotlin, Gradle, detekt or editorconfig files, the project SHALL run detekt, the
Konsist architecture tests and the project build with its JVM and host unit tests, and SHALL send the agent back to
fix any failure with a trimmed failure report. The check SHALL stop blocking after three consecutive blocked stops so
the agent can never be stuck in a loop.

#### Scenario: Failing static analysis
- **WHEN** the agent stops with a detekt violation in a changed file
- **THEN** the agent is not allowed to finish and is shown the violation

#### Scenario: Clean work
- **WHEN** the agent stops and every check passes
- **THEN** the agent finishes normally

#### Scenario: Nothing relevant changed
- **WHEN** the agent stops and no Kotlin, Gradle, detekt or editorconfig file changed
- **THEN** no check runs

#### Scenario: Repeated failure
- **WHEN** the agent has been blocked three times in a row for the same turn
- **THEN** the fourth stop is allowed and the remaining failure is reported

### Requirement: Check coverage and device tests before the agent pushes
When the agent pushes commits that change Kotlin, Gradle, detekt or editorconfig files, the project SHALL first run the
JaCoCo coverage floor checks, the iOS simulator tests and the Android instrumented tests on the project's own emulator,
cheapest first, and SHALL deny the push with a trimmed failure report when any of them fails. A pass SHALL be remembered for the commit it ran on and SHALL NOT
be committed. The check SHALL NOT use or modify an emulator of another project.

#### Scenario: Coverage floor not met
- **WHEN** the agent pushes and a module is below its coverage floor
- **THEN** the push is denied and the report names the module and the floor

#### Scenario: iOS simulator test fails
- **WHEN** the agent pushes and an iOS simulator test fails
- **THEN** the push is denied, the report names the failing test, and the emulator is not started

#### Scenario: Instrumented test fails
- **WHEN** the agent pushes and an Android instrumented test fails
- **THEN** the push is denied and the report names the failing test

#### Scenario: All checks pass
- **WHEN** the agent pushes and the coverage floors, the iOS simulator tests and the instrumented tests pass
- **THEN** the push goes ahead and the commit is recorded as checked

#### Scenario: Same commit pushed again
- **WHEN** the agent pushes a commit that was already recorded as checked
- **THEN** no check runs and the push goes ahead

#### Scenario: Nothing relevant pushed
- **WHEN** the agent pushes commits that change no Kotlin, Gradle, detekt or editorconfig file
- **THEN** no check runs

#### Scenario: Branch deletion or dry run
- **WHEN** the agent deletes a remote branch or runs a push with `--dry-run`
- **THEN** no check runs

#### Scenario: Other project's emulator is connected
- **WHEN** another project's emulator is running while the agent pushes
- **THEN** the instrumented tests run only on the VerbeClair emulator

### Requirement: Only the developer can run `/finalize`
The project SHALL provide a `/finalize` skill that runs detekt, the Konsist tests, the full build, the coverage floor
check, the Android instrumented tests on the project's own emulator, and the `/verify` review. The agent SHALL NOT be
able to invoke it; only the developer can. `/finalize` SHALL never use or modify an emulator of another project.

#### Scenario: Developer runs /finalize
- **WHEN** the developer types `/finalize`
- **THEN** every listed check runs and the result is either ready to push or a list of what failed

#### Scenario: Agent tries to run /finalize
- **WHEN** the agent attempts to invoke `/finalize` on its own
- **THEN** the skill is not available to it

#### Scenario: Other project's emulator is connected
- **WHEN** another project's emulator is running while `/finalize` runs
- **THEN** the instrumented tests run only on the VerbeClair emulator

### Requirement: `/finalize` records a pass for one commit
When every `/finalize` check passes, it SHALL record the commit it passed on. The record SHALL be local to the
developer's checkout and SHALL NOT be committed. When any check fails, `/finalize` SHALL NOT record a pass.

#### Scenario: All checks pass
- **WHEN** every `/finalize` check passes on a commit
- **THEN** that commit is recorded as passed

#### Scenario: A check fails
- **WHEN** any `/finalize` check fails
- **THEN** no pass is recorded and an earlier pass for another commit stays as it was

#### Scenario: New commit after a pass
- **WHEN** a new commit is made after `/finalize` passed
- **THEN** the recorded pass no longer matches the current commit

### Requirement: The agent opens a pull request only after a pass
The agent SHALL NOT open a pull request to `dev` unless `/finalize` has passed on the current commit, and SHALL
always state its base branch explicitly. When denied, the agent SHALL be told to ask the developer to run
`/finalize`. A pull request whose base is exactly `main` SHALL NOT be checked by this requirement.

#### Scenario: Pass recorded for the current commit
- **WHEN** the agent opens a pull request to `dev` and the recorded pass equals the current commit
- **THEN** the pull request is created

#### Scenario: No pass recorded
- **WHEN** the agent opens a pull request to `dev` and no pass is recorded
- **THEN** the command is denied and the message tells the agent to ask the developer to run `/finalize`

#### Scenario: Stale pass
- **WHEN** the agent opens a pull request to `dev` after a commit made since the recorded pass
- **THEN** the command is denied and shows both commits

#### Scenario: Base branch not stated
- **WHEN** the agent opens a pull request without stating the base branch
- **THEN** the command is denied and the message says to use `dev`

#### Scenario: Other commands
- **WHEN** the agent runs any command that does not open a pull request
- **THEN** this requirement does not affect it

### Requirement: The developer is never blocked
None of these checks SHALL block, delay or alter a `git` or `gh` command the developer types in their own terminal,
including `git push` and opening a pull request. The agent's own `git push` SHALL NOT require a `/finalize` pass,
and no git hook SHALL be installed to enforce these checks.

#### Scenario: Developer pushes by hand
- **WHEN** the developer runs `git push` in a terminal with no recorded pass
- **THEN** the push is not checked or blocked

#### Scenario: Developer opens a pull request by hand
- **WHEN** the developer runs the pull request command in a terminal with a stale pass
- **THEN** the pull request is created

#### Scenario: Agent pushes a work branch without a /finalize pass
- **WHEN** the agent runs `git push` for its work branch with no `/finalize` pass recorded
- **THEN** the push is not blocked for that reason; only the coverage and instrumented-test check can deny it

### Requirement: No PRISM remains
The repository SHALL NOT contain PRISM tooling, configuration, launchers, hooks, skills or naming, and the static
analysis tools SHALL keep enforcing the same rules through the project's own Gradle plugins.

#### Scenario: Search for PRISM
- **WHEN** the repository files are searched for "prism" outside version control history and build output
- **THEN** nothing is found, except where a text states that PRISM was removed

#### Scenario: Build after removal
- **WHEN** the full build runs from the repository root
- **THEN** detekt, ktlint, the Konsist tests and the unit tests run and pass
