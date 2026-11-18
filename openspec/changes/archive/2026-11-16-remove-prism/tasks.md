## 1. Rename away from PRISM

- [x] 1.1 Rename plugin ids `prism.*` to `verbeclair.*` in `build-logic` and every module that applies them; verify `./gradlew help` configures all modules
- [x] 1.2 Rename `:tooling:prism-rules` to `:tooling:detekt-rules` (folder, `settings.gradle.kts`, references) and its package to `com.vamvisions.verbeclair.detekt`, including the `META-INF/services` provider; verify `./gradlew :tooling:detekt-rules:test` passes
- [x] 1.3 Rename the `prism:` rule-set key in `detekt.yml` and any baselines that name it; verify `./gradlew detekt` reports no new findings
- [x] 1.4 Verify `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew build` passes, then commit the rename on its own

## 2. Delete PRISM-only Gradle code

- [x] 2.1 Add a task to the ktlint convention plugin that writes `build/ktlint/cli-classpath.txt` (moved from PRISM's plugin); verify the file exists and lists the ktlint CLI jars after running it
- [x] 2.2 Delete `StaticAnalysisConventionPlugin`, `PrismScope`, the `scope.json` reads and `prismKtlintBaseline`, keeping the existing baselines suppressing today's findings; verify `./gradlew detekt ktlintCheck :tooling:konsist:test` pass
- [x] 2.3 Verify the full `./gradlew build` still passes and commit

## 3. Remove the PRISM layer

- [x] 3.1 Delete `.prism/`, `prism`, `prism.cmd` and the PRISM blocks in `.gitignore` and `.gitattributes`; verify `git ls-files | grep -i prism` lists only files handled by later tasks
- [x] 3.2 Delete the skills `prism-setup` and `prism-burndown`; verify neither directory exists
- [x] 3.3 Strip PRISM hook entries, `permissions.ask` entries and `PRISM_AVD_PREFIX` from `.claude/settings.json`; verify the file is valid JSON and has no `prism` text
- [x] 3.4 Remove `prism.yml` references from the agents `skip-checker`, `convention-reviewer` and `mutation-worker` and make `/verify` a plain diff review; verify `grep -ri prism .claude/agents .claude/skills/verify` finds nothing
- [x] 3.5 Verify `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew build` still passes and commit

## 4. Hooks and the new `/finalize`

- [x] 4.1 Add `.claude/hooks/ktlint-format.sh` (PostToolUse `Edit|Write`) and register it; verify a mis-indented `.kt` edit is reformatted, a wildcard import is reported, a Markdown edit is ignored, and a missing classpath file exits 0
- [x] 4.2 Add `.claude/hooks/stop-checks.sh` (Stop) with the `stop_hook_active` guard, a counter capped at three blocks, and the relevant-files filter; verify a detekt violation blocks the stop, the fourth stop is allowed, and a Markdown-only change runs nothing
- [x] 4.3 Measure how long the stop checks take; if the iOS simulator tasks make them too slow, exclude them there and verify CI still runs them
  - Measured: a change in `:core:domain` ran detekt, ktlint, Konsist and every module's `check` (iOS simulator tests included) in 43 s; unchanged inputs take 7 s. Nothing is excluded.
- [x] 4.4 Change `pr-gate.sh` to read the marker from `.claude/state/finalize-pass`, reword the denial to ask the developer to run `/finalize`, and add `.claude/state/` to `.gitignore`; verify the updated `pr-gate-test.py` covers pass, no pass, stale pass, missing base and `--base main`, and passes
- [x] 4.5 Register `pr-gate.sh` as a PreToolUse Bash hook and verify no git hook is installed (the agent-side push hook is task 4.8)
- [x] 4.6 Rewrite `.claude/skills/finalize/SKILL.md` with `disable-model-invocation: true`: detekt, Konsist, `build`, `jacocoCoverageVerification`, `scripts/emulator/verbeclair-emulator.sh start phone`, `ANDROID_SERIAL=emulator-5574 ./gradlew connectedAndroidDeviceTest`, `/verify`, then write `HEAD` to the marker only if all passed; verify a run with a failing check leaves the marker unchanged and a passing run writes the current commit
- [x] 4.7 Verify the agent cannot invoke `/finalize` and that a terminal `git push` and PR command are not blocked by any hook (Claude hooks only apply to the agent's own tool calls)
- [x] 4.8 Add `.claude/hooks/push-checks.sh` (PreToolUse `Bash`, `git push`) and `push-checks-test.py`; verify with stubbed Gradle and emulator scripts that a failing coverage floor, a failing iOS simulator test (emulator never started) and a failing device test deny the push, a pass records `.claude/state/push-pass`, and that a Markdown-only push, a deletion, a dry run and a repeated `HEAD` run nothing
- [x] 4.9 Register it after `pr-gate.sh` with a one-hour timeout; verify the settings file is valid JSON and `jacocoCoverageVerification` runs for real from the repository root
- [x] 4.10 Make `pr-gate.sh` remind the agent to run `/verify` and compare changed screens with Figma whenever it lets a PR through; verify `pr-gate-test.py` checks the reminder is printed for every allowed PR and never for a denial or another command

## 5. Docs, memory and requirement

- [x] 5.1 Rewrite AGENTS.md rule 9 for the new `/finalize` and state the agent must never write the marker by hand, replace the PRISM emulator paragraph with one naming `/finalize`, and describe the three hooks and CI as the PR gate; verify no `prism` text remains in AGENTS.md and CLAUDE.md
- [x] 5.2 Show the user the reworded Notion N-16 text and, once approved, update the N-16 row; verify by fetching the page
- [x] 5.3 Update the memory files `prism-kmp-plan`, `prism-concepts`, `verbeclair-emulators`, `remove-prism-plan` and `MEMORY.md`; verify each index line matches its file

## 6. Done gate

- [x] 6.1 Verify `grep -rIi prism . --exclude-dir=.git --exclude-dir=build --exclude-dir=archive` finds nothing, except text stating PRISM was removed
- [x] 6.2 Verify `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew build`, `./gradlew jacocoCoverageVerification` and `ANDROID_SERIAL=emulator-5574 ./gradlew connectedAndroidDeviceTest` all pass for every module
  - Device tests ran on `emulator-5554`: the VerbeClair AVD was already running on the default port, so 5574 was not used.
- [ ] 6.3 Run `/finalize` as the developer, then open the PR with `--base dev` for the user's approval; verify CI is green and never merge locally
