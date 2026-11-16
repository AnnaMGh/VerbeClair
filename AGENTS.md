# AGENTS.md

**Before doing any work in this repository, read [`openspec/config.yaml`](openspec/config.yaml).**
It is the single, authoritative source for project context, sources of truth (Figma, Notion),
per-artifact rules and the definition of done. If anything here disagrees with it, `config.yaml` wins.

@openspec/config.yaml

## Non-negotiables (summary — details in `openspec/config.yaml`)

1. **Sources of truth** — Design: Figma. Requirements and API: Notion. Check both before any decision;
   if neither specifies it, ask the user instead of guessing.
2. **Figma MCP is mandatory** — if the Figma MCP tools cannot connect, stop and fail with an error.
   Never improvise a design.
3. **Implement from real Figma metadata** (`get_metadata`, `get_design_context`, `get_variable_defs`),
   never from screenshots of the mocks. Screenshots are allowed only to verify a finished
   implementation against Figma.
4. **Reuse the design system** — tokens come from Figma Foundation/Components; check for an existing
   component before creating one. Icons come from Figma as SVG (via `figma-to-android-vector`);
   fall back to Material 3 icons only if the icon is not in Figma.
5. **Previews** — every component and screen has one `@Preview` per state, all states covered, each
   preceded by a `// Figma: <url-of-that-state>` comment.
6. **Targets** — phone portrait, phone landscape, tablet; light and dark mode.
7. **Definition of done** — all tests pass and `./gradlew build` passes for every affected module
   (including modules that depend on the edited one), not just the module edited.
8. **Branching** — `main` ← `dev` ← work branch. Every feature or change gets a new branch created
   from `dev`; work branches merge into `dev`, and only `dev` merges into `main`. Never commit
   directly to `main` or `dev`. Merges into `dev` and `main` happen only through a GitHub pull
   request approved by the user — never merge locally or push to `dev`/`main` directly.
9. **`/finalize` before a PR to `dev`** — only the developer can run `/finalize` (the agent cannot invoke it).
   It runs detekt, the Konsist tests, `./gradlew build`, `jacocoCoverageVerification`, the Android device tests on the
   VerbeClair emulator and `/verify`, stops at the first failure, and on a full pass writes the commit it passed to
   `.claude/state/finalize-pass` (gitignored). **The agent must never write that file by hand.** When a PR to `dev` is
   due and there is no pass for the current `HEAD`, ask the developer to run `/finalize`, then open the PR with
   `gh pr create --base dev`. A new commit makes the pass stale, so it has to run again.
   Four Claude-only hooks and CI make up the gate: `ktlint-format.sh` formats each edited Kotlin file,
   `stop-checks.sh` runs detekt, ktlint, Konsist and the unit tests when the agent stops after changing Kotlin or
   Gradle files (it blocks at most three times in a row), `push-checks.sh` runs the coverage floors, the iOS simulator
   tests and the device tests on `emulator-5574` before the agent's `git push` (it needs no `/finalize` pass, skips pushes with no Kotlin or
   Gradle change, and remembers a passed `HEAD` in `.claude/state/push-pass`), and `pr-gate.sh` denies the agent's PR
   to `dev` without a pass; when it lets a PR through it reminds the agent to run `/verify` and to compare every
   changed screen with its Figma frame, and to say in the PR description what was checked. None of them touches a `git` or `gh` command the developer types in a terminal, and no git
   hook is installed.
   `pr-gate.sh` is a plain text match: any Bash command whose text contains `gh … pr create` or `gh … pr new` is
   treated as the PR command, however it is wrapped. That also catches text that only mentions it (a commit message,
   an echo); write such text with Edit/Write, or run the real PR command. Always pass `--base` explicitly: the gate is
   skipped only when every `--base`/`-B` in the text is exactly `main`, and any other value (even an unrelated `-B`)
   is gated. A base built from quoted pieces (`--base d"ev"`) is read as written and is a known gap. It does not cover
   `gh api` and it checks the checked-out `HEAD`, so create the PR from the branch it is for, without `--head`. It is
   a guardrail against forgetting, not a security boundary; CI is the gate that cannot be skipped.

   **Order for a change made with OpenSpec:** finish the tasks, run the tests and `./gradlew build` (this ticks the
   done-gate task) and commit → archive the change and commit it (`chore(openspec): archive <change>`) → ask the
   developer to run `/finalize` → fix and commit until it records a pass → open the PR to `dev`. Archive before
   `/finalize`, not after: the archive is a commit, so one made after the pass would make it stale. The PR then ships
   the archived change and the synced specs. `/finalize` is an extra review gate and does not replace the build and
   tests; if it forces code fixes, re-run them and edit the archived change only when its artifacts became wrong.

## Running builds and tests

- **iOS needs Xcode, set per command.** If `xcode-select -p` points at
  `/Library/Developer/CommandLineTools`, `simctl` is missing and the `iosSimulatorArm64Test` tasks fail
  (`xcrun` exit 72). Prefix the command instead of changing `xcode-select` or the shell profile:
  `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew build`.
- **`./gradlew build` does not run the Android device tests.** `commonTest` runs on Android as an
  instrumented test, so "all tests pass" also needs a running emulator/device and
  `./gradlew connectedAndroidDeviceTest`.

## Emulators (this project only)

Several projects share this machine, so VerbeClair uses its own emulators, all named `VerbeClair_*`. They
mirror the CI job (`pixel_6`, API 34, `google_apis`, headless, `swiftshader_indirect`, animations off); only
the CPU differs (arm64 here, x86_64 on the CI runner).

| AVD | Start | Serial |
|---|---|---|
| `VerbeClair_pixel_6_API_34` | `scripts/emulator/verbeclair-emulator.sh start phone` | `emulator-5574` |
| `VerbeClair_pixel_tablet_API_34` | `scripts/emulator/verbeclair-emulator.sh start tablet` | `emulator-5576` |

- Run device tests on a VerbeClair emulator only: `ANDROID_SERIAL=emulator-5574 ./gradlew connectedAndroidDeviceTest`.
  Without `ANDROID_SERIAL`, Gradle runs on every connected device, including other projects' emulators.
- `/finalize` starts the phone emulator with `scripts/emulator/verbeclair-emulator.sh start phone` and runs the device
  tests with `ANDROID_SERIAL=emulator-5574`, so it never touches another project's emulator.
- Never use, stop or modify another project's emulator.
