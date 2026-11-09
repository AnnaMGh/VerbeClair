## Context

`dev` still carries the first PRISM install (0.6.x): `prism.ktlint`, `prism.detekt`, `prism.jacoco` and `prism.static-analysis` convention plugins in `build-logic`, a Konsist module `tooling/konsist`, and a rule module `tooling/prism-rules`. See proposal.md - Why for the gaps. Facts that shape the approach:

- Konsist's `ProjectScope` builds one scope for the whole repository and defines `productionFiles` as files whose source set is named `main` and `testFiles` as everything else. KMP names its source sets `commonMain`, `androidMain`, `iosMain`, `jvmMain`, `commonTest`, `iosTest`, `androidHostTest` and `androidDeviceTest`.
- `detekt.yml` has about 93 lines of test patterns (`**/test/**`, `**/androidTest/**`); `.editorconfig` has one test override, `[**src/{test,androidTest}/**/*.kt]`.
- `ktlintCheck` is a `JavaExec` that nothing in `check` depends on. The root `staticAnalysis` aggregate runs it, but CI calls `./gradlew build`.
- JaCoCo: the plugin pins the tool version and enables Android coverage flags, but KMP modules have no report task. Only `:core:domain` and `:core:database` declare a `jvm()` target, so only they can produce JVM unit-test coverage. A report logic that picks the first variant that wrote execution data (JVM, else Android host) already exists on the unpushed branch `chore/prism-kmp-support` (`JacocoConventionPlugin.kt`, +86 lines) and produced the figures in the proposal (58.4 % and 44.4 %).

## Goals / Non-Goals

**Goals:**
- After this change, the three checks see all of the KMP source sets, and CI fails on a violation in any of them.
- Coverage can only go up, and is enforced by Gradle, with no external tool.

**Non-Goals:**
- Removing or renaming anything PRISM-related; that is `remove-prism`, which depends on this change.
- Giving more modules a JVM test target. `:shared` and the others stay exempt (a `jvm()` target on `:shared` would need one on four dependencies, with JVM implementations of Room, DataStore and platform paths, for a composition root).
- Fixing the flaky `AppKoinGraphTest`, and changing the 80 % target.

## Decisions

**1. Konsist classifies by source-set name suffix.** Production is a source set whose name ends in `Main`, test is one whose name ends in `Test`. Alternative: list the names. A suffix keeps working when a target is added (`jvmMain`, `desktopMain`). `ScopeIntegrityTest` gains an assertion that every module that has Kotlin in `src/*Main` has at least one production file in scope, so a future rename that empties the scope fails loudly instead of passing rules over nothing. Before flipping it, run the Konsist tests once with the new classification and list what fails: fix real violations, and for a rule that cannot be satisfied yet add a narrow, commented allow-list entry. A wholesale `@Ignore` or a weaker rule is not allowed.

**2. One pattern set for "test source sets", applied by script.** Replace each `**/test/**` and `**/androidTest/**` entry in `detekt.yml`, and the `.editorconfig` section, with the same list: `test`, `androidTest`, `commonTest`, `iosTest`, `androidHostTest`, `androidDeviceTest`. Done with one scripted pass and a diff review, not by hand. Alternative: a catch-all like `**/*Test/**`; rejected because it would also match production directories that happen to end in `Test`. New findings from the wider patterns follow the same rule as decision 1: fix, or baseline in the module's existing baseline file with the finding visible in the diff.

**3. ktlint joins `check` through the plugin.** `check` of every module that applies the ktlint plugin depends on its `ktlintCheck`. Alternative: add the aggregate `staticAnalysis` to CI; rejected, because it keeps a second entry point that `remove-prism` deletes, and `build` is already the CI contract.

**4. Coverage verification is Gradle-native and not part of `check`.** The report logic from `chore/prism-kmp-support` is brought over with `git checkout chore/prism-kmp-support -- build-logic/src/main/kotlin/JacocoConventionPlugin.kt`, then extended with a `jacocoCoverageVerification` task (JaCoCo's own violation rule, counter LINE, minimum = the module's floor) that depends on the same test tasks. Floors are declared in the module build file next to the plugin application; the 80 % target is one constant in the plugin, and the exempt modules are listed in one place in the plugin with a reason each. Alternatives: keep PRISM's Python coverage gate (removed in the next change); attach verification to `check` (every local build and every Stop hook would run the JVM tests twice and slow down). CI gets a separate step, so a coverage failure is distinguishable from a test failure.

**5. The ratchet is a convention plus a visible diff.** Floors live in version-controlled build files, so lowering one shows in the pull request. Gradle cannot know the previous value; a script that compares floors with the base branch is possible later but is not built here. Stated requirement: lowering needs a reason in the pull request description.

## Risks / Trade-offs

- [Newly live Konsist and detekt rules find many real violations] → triage in the spike before editing anything; fix what is small, baseline what is not, and record the count in the pull request so the debt is visible.
- [`jacocoCoverageVerification` measures one variant, so coverage from the other (JVM vs Android host) is ignored] → documented in the plugin; acceptable because both exempt and measured modules use one variant today.
- [Floors at today's value make the gate tight; a harmless refactor can drop coverage by a fraction] → floors are rounded down to the whole percent.
- [ktlint in `check` slows local builds slightly and may surface existing formatting debt] → ktlint baselines per module already exist; findings not covered by them are fixed with `ktlintFormat` in the same change.
- [Branch contains tooling from an unpushed branch] → only one file is taken from it; nothing else of the PRISM upgrade is merged.

## Migration Plan

Work on `chore/fix-kmp-static-analysis` in this order, running `./gradlew ktlintCheck detekt :tooling:konsist:test :tooling:prism-rules:test` after each step: Konsist scope, detekt and `.editorconfig` patterns, ktlint in `check`, coverage verification, CI step. Roll back by reverting the merge commit; no data or API is touched.

## Open Questions

- Whether any exempt module can cheaply gain a host test target later (for example `:core:presentation`, which has only `commonMain` and `commonTest`). Does not change this change's specs or tasks.
