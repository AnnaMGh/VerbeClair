## Context

See proposal.md for why. Today PRISM runs detekt, ktlint and Konsist through its own plugins
(`StaticAnalysisConventionPlugin`, `PrismScope`, `scope.json`), a hook launcher (`.prism/adapters/lib/prism-hook`)
registered for four events in `.claude/settings.json`, and a PRISM `/finalize` skill that writes
`.prism/verify/state/finalize-pass`. `.claude/hooks/pr-gate.sh` already denies the agent's PR to `dev` unless that
marker equals `HEAD` and already requires `--base`; it is a plain text match on the Bash command and is skipped
only when every base is exactly `main`. The three engines are plain Gradle tasks and CI runs `./gradlew build`
without PRISM, so CI is unaffected.

## Goals / Non-Goals

**Goals:**
- No PRISM code, name or state left; the same rules keep enforcing through VerbeClair-named Gradle plugins.
- Four Claude-only hooks plus a developer-only `/finalize` as specified in `agent-quality-gates`.
- Reuse what works: `pr-gate.sh` and its tests, `scripts/emulator/verbeclair-emulator.sh`, the existing engines.

**Non-Goals:**
- Changing which rules run or any baseline content; CI changes; the flaky `AppKoinGraphTest` fix (own branch).
- A git `pre-push` hook or any gate on the developer's terminal.

## Decisions

1. **Keep `pr-gate.sh`, change only its marker path.** The marker moves to `.claude/state/finalize-pass`
   (gitignored) and the denial text says to ask the developer to run `/finalize`. Alternative: write a new hook;
   rejected, the current one is tested (`pr-gate-test.py`) and already fails closed on the base branch.
2. **`/finalize` is a rewritten skill with `disable-model-invocation: true`.** That is what keeps the agent from
   running it. The marker is written by a final step of the skill, only after every check passed, from `git rev-parse
   HEAD`. Alternative: a script the agent could also call; rejected because the pass must come from the developer.
   This is a convention guardrail, not a security boundary: the agent could in principle write the marker by hand,
   and AGENTS.md says it must not.
3. **`/finalize` order, cheapest first:** detekt and `:tooling:konsist:test`, `./gradlew build`,
   `jacocoCoverageVerification`, start the phone emulator with `scripts/emulator/verbeclair-emulator.sh start phone`,
   `ANDROID_SERIAL=emulator-5574 ./gradlew connectedAndroidDeviceTest`, then `/verify`. It stops at the first failure
   and never writes the marker unless all pass. iOS tasks use `DEVELOPER_DIR` per command, never `xcode-select`.
4. **Hooks are POSIX sh in `.claude/hooks/`:** `ktlint-format.sh` (PostToolUse `Edit|Write`) and `stop-checks.sh`
   (Stop), next to `pr-gate.sh` and `push-checks.sh` (both PreToolUse `Bash`). The format hook needs the ktlint CLI classpath, so the ktlint
   convention plugin gets a task that writes `build/ktlint/cli-classpath.txt` (moved from PRISM's plugin). Alternative:
   run `./gradlew ktlintFormat` per edit; rejected as too slow per edit.
5. **Stop hook loop guard:** read `stop_hook_active` and keep a counter file per session; allow the stop after three
   blocks. It runs only when a changed file matches Kotlin, Gradle, `detekt.yml` or `.editorconfig`, and runs `build`
   with the JVM and host unit tests. If iOS simulator tasks make it too slow it excludes them (CI still runs them);
   this is measured while implementing.
6. **Rename in one mechanical commit before any deletion:** `prism.*` plugin ids to `verbeclair.*`,
   `:tooling:prism-rules` to `:tooling:detekt-rules`, package `com.plcoding.prism...` to
   `com.vamvisions.verbeclair.detekt`, the `prism:` rule-set key in `detekt.yml`, and the `META-INF/services`
   provider. Then delete PRISM-only Gradle code, then the PRISM layer, so a failure is easy to attribute.
7. **Baselines are kept as they are.** PRISM's report-only mode goes; the plugins enforce, and the existing baseline
   files keep today's findings suppressed.
8. **Docs:** AGENTS.md rule 9 is rewritten for the new `/finalize` and the PRISM emulator paragraph is replaced by
   one that names `/finalize`; the `agent-quality-gates` spec is the behavior contract. Notion N-16 is reworded after
   the user approves the text.

9. **`push-checks.sh` gates the agent's `git push`, not its pull request.** It runs `jacocoCoverageVerification`, then
   starts the phone emulator and runs `ANDROID_SERIAL=emulator-5574 ./gradlew connectedAndroidDeviceTest`, and denies
   the push on a failure with a trimmed report. It is a plain text match on `git push` like `pr-gate.sh`. It skips
   branch deletions and dry runs, pushes whose commits (against the upstream, else `origin/dev`) change no Kotlin,
   Gradle, detekt or editorconfig file, and a `HEAD` already recorded in `.claude/state/push-pass` (gitignored), so a
   retried push does not repeat the run. It does not read the `/finalize` marker: the two gates are independent.
   Alternative: only run these checks in `/finalize`; rejected by the user, who wants the agent's push checked too.

## Risks / Trade-offs

- [The pass marker can be written by hand] → documented as forbidden in AGENTS.md; CI remains the real gate.
- [Text match in `pr-gate.sh` also catches commands that only mention PR creation] → known and accepted; the denial
  explains it, and the agent writes such text with Edit/Write.
- [The push hook can take minutes: emulator boot plus device tests] → skipped for pushes with no Kotlin or Gradle change and for a `HEAD` that already passed; the hook timeout is one hour.
- [Stop hook slows every turn that touches Kotlin] → runs only on relevant changes, loop-capped at three blocks.
- [Rename touches many files] → one mechanical commit, verified by a green `./gradlew build` and a `grep` for
  "prism" before moving on.
- [Flaky `AppKoinGraphTest` can fail `/finalize` and the CI device job] → fix on its own branch; until then rerun.

## Migration Plan

Order of commits on `chore/remove-prism`: rename, move the ktlint classpath task, delete PRISM-only Gradle code,
delete the PRISM layer and settings entries, add the hooks and the new `/finalize`, docs. Each step ends with
`DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew build` green. Rollback is reverting the branch
(or not merging the PR); nothing outside the repository changes except Notion N-16, edited last.
