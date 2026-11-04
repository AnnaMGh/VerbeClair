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
9. **`/finalize` before a PR to `dev`** — run `/finalize` and get `READY_TO_PUSH` (it ends in `/verify`)
   before `gh pr create --base dev`. It records the commit it passed in
   `.prism/verify/state/finalize-pass`; `.claude/hooks/pr-gate.sh` denies the PR if that is not the current
   `HEAD`, so a new commit means running it again. A change that touches no module (a hook, script, workflow or
   docs change) ends in `NOTHING_TO_FINALIZE` after `/verify` passes, and that records the pass too. Always pass
   `--base` explicitly. The hook guards `gh pr create` (also behind `bash -c`, `command`, a path or a brace group).
   It does not cover `gh api` and it checks the checked-out `HEAD`, so create the PR from the branch it is for,
   without `--head`. It is a guardrail against forgetting, not a security boundary.

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
- `PRISM_AVD_PREFIX=VerbeClair` (set in `.claude/settings.json`) makes `.prism/assets/select_emulator.py`, and so
  `/finalize`, consider only these AVDs. Start the emulator with the script first: if none is running,
  `/finalize` boots one with `android emulator start`, which does not use the CI options or the fixed port.
- Never use, stop or modify another project's emulator.
