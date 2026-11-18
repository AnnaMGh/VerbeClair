---
name: finalize
description: Developer-only gate before a pull request to dev. Runs scripts/finalize.sh (detekt, ktlint, Konsist, the full build, the coverage floor and the Android instrumented tests on the VerbeClair emulator, which records the commit it passed on), then the /verify review. Ends in READY_TO_PUSH or BLOCKED.
disable-model-invocation: true
user-invocable: true
---

# /finalize — is this commit ready for a PR to `dev`?

Only the developer runs this (`disable-model-invocation: true`). The agent cannot invoke it, and must never write the
pass marker by hand. `.claude/hooks/pr-gate.sh` denies the agent's `gh pr create --base dev` unless the marker holds
the current `HEAD`.

The mechanical steps are `scripts/finalize.sh`, which decides the pass by exit codes alone and is the only thing that
writes `.claude/state/finalize-pass` (gitignored). The one judgement step, the `/verify` review, runs after it. Do not
fix code from inside this skill; fixing is a separate turn, and any new commit makes a pass stale.

## Steps

1. **Mechanical checks.** Run `scripts/finalize.sh` from the repository root. It runs, cheapest first and stopping at
   the first failure: a clean-tree check, `detekt ktlintCheck :tooling:konsist:test`, `./gradlew build`,
   `jacocoCoverageVerification`, the VerbeClair phone emulator and `connectedAndroidDeviceTest` on `emulator-5574`
   (rerun once when `AppKoinGraphTest` is the only failure). It sets `DEVELOPER_DIR` itself and never touches another
   project's emulator. On a failure it prints `BLOCKED at step <n>: <reason>` with the failing lines and writes
   nothing; end this skill in `BLOCKED` with that output. On success it records the commit and prints `READY_TO_PUSH`.
   If the agent's own run of the script is denied, ask the developer to run `! scripts/finalize.sh` and continue from
   its output.
2. **Review.** Invoke the `/verify` skill on the diff against `dev` (`--base=dev`). It passes when it reports no
   blocking finding (HIGH-confidence BLOCKER or MAJOR). On a blocking finding, **remove the marker** with
   `rm -f .claude/state/finalize-pass` so the pass does not outlive a failed review, and end in `BLOCKED`, with the
   finding. Removing the marker is the only thing the agent does to it.

## Report

End with one line, then a short table of the steps and their result:

- `READY_TO_PUSH <short sha>` — the script recorded the pass and `/verify` found nothing blocking. Say that the agent
  can now open the PR with `--base dev`, and that any new commit makes the pass stale.
- `BLOCKED at step <n>: <reason>` — nothing was recorded, or the marker was removed after a blocking review finding.
