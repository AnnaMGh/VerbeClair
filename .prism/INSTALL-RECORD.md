# PRISM install record

Installed 2026-10-20 into VerbeClair, harness Claude Code, from PRISM 0.6.3.

## 1. What landed
**Theirs** (yours to edit): `tooling/` (prism-rules, konsist), `build-logic/`, `detekt.yml`, `.editorconfig`,
`.gitignore`/`.gitattributes` (appended), `gradle/libs.versions.toml`, `settings.gradle.kts`, root and module
`build.gradle.kts`, every module's `detekt.baseline.xml` / `ktlint.baseline.xml`, `.claude/settings.json`,
`.claude/agents`, `.claude/skills` (finalize, prism-burndown), `./prism`, `./prism.cmd`.
**PRISM's**: `.prism/`.

Deviations from the shipped payload (deliberate):
- `build-logic/.../DetektConventionPlugin.kt`: detekt source set widened to every `src/<sourceSet>/{java,kotlin}`;
  the default (`src/main`, `src/test`) read none of this Kotlin Multiplatform code (NO-SOURCE).
- `tooling/*/build.gradle.kts`: `jvmToolchain(11)` removed (no JDK 11 here; CI uses 21).
- `gradle/libs.versions.toml`: added `coroutines = "1.11.0"` only because prism-doctor checks that key; the
  repository's own pin is `kotlinx-coroutines`.
- `ktlintFormat` reformatted 108 existing source files (uncommitted).
- Kept as the repository's own (PRISM's copy beside as `.prism-new`): `.claude/agents/{convention-reviewer,
  mutation-worker,verify-audit}.md`, `.claude/skills/verify/SKILL.md`.

## 2. Parameters
Answered/derived: PACKAGE_ROOT=com.vamvisions.verbeclair; MODULE_PACKAGE_ROOTS (8 modules);
LAYER_PURITY_DOMAIN_PACKAGE=com.vamvisions.verbeclair.core.domain; HARNESS=claude-code.
Chosen by user: posture = measure and decide per engine; pre-push floor installed (after first declining it).
Defaulted by decision: BASE_BRANCH=dev (origin/HEAD says main; work branches target dev).
Branch: installed on feature/ci-workflow at the user's choice.

## 3. Probe output (verbatim)
```
PRISM design-system probe

  PALETTE_OBJECT         none                     no declaration holds three or more raw Color literals
  EXTENDED_TOKEN_HOLDER  none                     found at core/designsystem/src/commonMain/kotlin/com/vamvisions/verbeclair/core/designsystem/theme/Color.kt:118, unused: PALETTE_RULES_ACTIVE=false
  THEME_OBJECT           VerbeClairTheme          core/designsystem/src/commonMain/kotlin/com/vamvisions/verbeclair/core/designsystem/theme/Theme.kt:25
  PALETTE_RULES_ACTIVE   false                    
  THEME_RULES_ACTIVE     true                     

```

## 4. Checked by no rule
`PALETTE_RULES_ACTIVE=false`: colours are top-level `val`s with no palette declaration, so ThemeColorDirectUse
and UnwiredThemeColor are off (70 of 72 rules active). Raw-colour use via named `val`s is checked by nothing;
`RawColorLiteral` catches hex literals only.
Coverage observes (nothing has measured it); gate 3 skips until `./prism promote --engine coverage`.
Konsist found no violations; several rules reported SKIPPED as not applicable (no Screen composables / ViewModels
matching the shapes yet).

## Posture
detekt + ktlint + konsist enforce; coverage observes. 323 detekt findings baselined across 8 modules
(`./prism baseline group`); a new violation of a baselined rule still fails.
Part 4 canary: a planted `println` failed `staticAnalysis` (NoConsoleLogging) and was removed.
