---
name: finalize
description: Developer-only gate before a pull request to dev. Runs detekt, the Konsist tests, the full build, the coverage floor, the Android instrumented tests on the VerbeClair emulator and the /verify review, then records the commit it passed on. Ends in READY_TO_PUSH or BLOCKED.
disable-model-invocation: true
user-invocable: true
---

# /finalize — is this commit ready for a PR to `dev`?

Only the developer runs this (`disable-model-invocation: true`). The agent cannot invoke it, and must never write the
pass marker by hand. `.claude/hooks/pr-gate.sh` denies the agent's `gh pr create --base dev` unless the marker holds
the current `HEAD`.

Run every step below from the repository root, in order, **cheapest first**. **Stop at the first failure**: report
what failed, with the trimmed output, and end in `BLOCKED`. Never write the marker unless every step passed. Do not
fix code from inside this skill; fixing is a separate turn, and any new commit makes a pass stale.

## Rules

- iOS tasks need Xcode, set **per command**: prefix Gradle with
  `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer`. Never run `xcode-select` or edit a shell profile.
- Use the VerbeClair emulator only: `emulator-5574`. Never use, stop or modify another project's emulator, and always
  pass `ANDROID_SERIAL`, because without it Gradle runs on every connected device.
- Stage output is long. Show only the failing lines on a failure and one line per passing stage.

## Steps

0. **Clean tree.** `git status --porcelain` must print nothing. The pass is recorded for `HEAD`, so uncommitted changes
   would not be covered by it. If it is not clean, end in `BLOCKED` and say to commit first.
1. **Static analysis and architecture:**
   `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew --continue detekt ktlintCheck :tooling:konsist:test`
2. **Full build:** `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew build`
3. **Coverage floor:** `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew jacocoCoverageVerification`
4. **Emulator:** `scripts/emulator/verbeclair-emulator.sh start phone` (it waits for boot and is a no-op when
   `emulator-5574` is already running). If it fails, end in `BLOCKED` with its output.
5. **Instrumented tests:**
   `ANDROID_SERIAL=emulator-5574 DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew connectedAndroidDeviceTest`
   `AppKoinGraphTest` is known to be flaky; if it is the only failure, rerun once and say so in the report.
6. **Review:** invoke the `/verify` skill on the diff against `dev`. It passes when it reports no blocking finding
   (HIGH-confidence BLOCKER or MAJOR). Any blocking finding ends in `BLOCKED`, with the finding.

## Recording the pass

Only when steps 0–6 all passed, and `git rev-parse HEAD` is still the commit the steps ran on:

```sh
mkdir -p .claude/state && git rev-parse HEAD > .claude/state/finalize-pass
```

The file is gitignored and local to this checkout. When any step failed, do not touch it: an earlier pass for another
commit stays as it was.

## Report

End with one line, then a short table of the steps and their result:

- `READY_TO_PUSH <short sha>` — all steps passed and the marker was written. Say that the agent can now open the PR
  with `--base dev`, and that any new commit makes the pass stale.
- `BLOCKED at step <n>: <reason>` — nothing was recorded.
