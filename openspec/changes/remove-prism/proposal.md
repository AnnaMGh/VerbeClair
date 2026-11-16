## Why

PRISM wraps detekt, ktlint and Konsist in an orchestration layer (doctor, `/finalize`, `pr-gate.sh`, `prism.yml`,
scope and baseline state) that fights Kotlin Multiplatform: it needed workarounds (`baseBranch: main`, explicit
`--base`) and broke on the 0.6.3 to 1.0.0 upgrade. The three engines are already plain Gradle tasks and CI already
runs `./gradlew build` without PRISM, so the orchestration only adds cost. The KMP gaps in the engines were fixed
by `fix-kmp-static-analysis` (archived 2026-11-12), so PRISM can now be removed.

Requirement: Notion **N-16 "Code quality gates"** in the
[VerbeClair requirements](https://app.notion.com/p/VerbeClair-requirements-3f1d02620bc1804180a7f241233fdacb)
(section 14, non-functional requirements): ktlint, detekt and Konsist run from the Gradle build that CI runs, coverage
has per-module floors with a target of 80 %, and four automatic agent checks run cheapest first (after an edit, at
stop, before a push, before a pull request). CI stays the gate for everyone; the agent checks never block a person's
own pushes or pull requests.

**Open point:** N-16 lists a check "before the agent pushes". This change moves the slow checks into the developer's
`/finalize` and gates only the agent's pull request on its pass, so N-16 needs rewording (text to be approved by the
user before Notion is edited).

## What Changes

- **BREAKING** Remove PRISM: `.prism/`, the `prism` and `prism.cmd` launchers, the PRISM `pr-gate*` hook, the skills
  `prism-setup` and `prism-burndown`, the PRISM hooks, `permissions.ask` entries and `PRISM_AVD_PREFIX`
  in `.claude/settings.json`, and the PRISM blocks in `.gitignore` and `.gitattributes`. The PRISM-based
  `/finalize` and its `.prism/verify/state/finalize-pass` marker go.
- Replace `/finalize` with an own skill that only the developer can run (`disable-model-invocation`). It runs
  detekt, Konsist, `./gradlew build`, the coverage floors and the Android device tests on `emulator-5574`, then
  `/verify`, and on success records the passing commit in a gitignored marker. AGENTS.md rule 9 is rewritten for it.
- Rename away from "prism": plugin ids `prism.*` to `verbeclair.*`, `:tooling:prism-rules` to
  `:tooling:detekt-rules`, package `com.plcoding.prism...`, the `prism:` key in `detekt.yml` and the
  `META-INF/services` provider. The custom rules are kept.
- Delete PRISM-only Gradle code (`StaticAnalysisConventionPlugin`, `PrismScope`, `scope.json` reads,
  `prismKtlintBaseline`). Move the ktlint classpath task into the ktlint plugin, because the format hook needs it.
- Add three POSIX-sh hooks in `.claude/hooks/`, active only for Claude's own tool calls (no git `pre-push` hook):
  1. after `Edit`/`Write` of a Kotlin file: ktlint formats that file;
  2. at stop: detekt, Konsist, build and JVM unit tests, loop-guarded;
  3. before Claude opens a pull request: denied unless `/finalize` passed for the current commit and `--base` is
     given. Claude's own `git push` is not gated.
- A developer's own `git push` and pull request from a terminal are never checked or blocked.
- Keep `/verify` as a plain diff review and strip `prism.yml` references from the agents `skip-checker`,
  `convention-reviewer` and `mutation-worker`.
- Update AGENTS.md (describe the hooks, CI as the PR gate, drop the PRISM emulator paragraph).

## Capabilities

### New Capabilities
- `agent-quality-gates`: the three Claude-only hooks, the developer-only `/finalize` and its pass marker, what lets
  the agent open a pull request, and that nothing blocks a person's own git or `gh` commands.

### Modified Capabilities
<!-- None. The continuous-integration spec already requires ktlint, detekt, Konsist and coverage floors in CI and
     does not mention PRISM, so CI behaviour does not change. -->

## Impact

- **Figma:** no screens or components are affected; no UI changes.
- **Code:** `build-logic` convention plugins (rename, delete, ktlint classpath task), `tooling/prism-rules`
  (renamed, package changed), `tooling/konsist`, `detekt.yml`, `settings.gradle.kts`, module `build.gradle.kts`
  files that apply `prism.*` plugins.
- **Agent tooling:** `.claude/settings.json`, `.claude/hooks/`, `.claude/skills/`, `.claude/agents/`, `AGENTS.md`.
- **CI:** `.github/workflows/ci.yml` should need no change; verified by a green run.
- **Out of scope:** the flaky `AppKoinGraphTest.verbCatalogReadsTheInstalledBundledDatabase`, which can randomly
  fail hook 3 and the CI device job, gets its own small branch.
