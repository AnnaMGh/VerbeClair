#!/bin/sh
# PreToolUse(Bash) hook: before the agent's `git push`, run the JaCoCo coverage floors, the iOS simulator tests and
# the Android instrumented tests on the VerbeClair emulator, and deny the push when any of them fails. Cheapest first,
# so a failure never waits for the emulator to boot. The iOS tests are the simulator tests CI runs in its own job
# (commonTest and iosTest, Compose UI tests included); the project has no XCUITest suite.
#
# Reads the hook event (JSON) on stdin. Exit 2 with a trimmed failure report on stderr denies the command; exit 0
# allows it. Only commands whose text contains `git push` are looked at (a plain text match, like pr-gate.sh: text
# that only mentions the command is also checked, and costs nothing once the HEAD has passed). Never active for a
# push the developer types in a terminal.
#
# Skipped (exit 0): branch deletions and dry runs; a push whose commits change no Kotlin, Gradle, detekt or
# editorconfig file; a HEAD that already passed (recorded in .claude/state/push-pass, gitignored).
#
# The device tests run on emulator-5574 only (ANDROID_SERIAL), never on another project's emulator.
set -u

root=${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel 2>/dev/null || pwd)}
state_dir="$root/.claude/state"
marker="$state_dir/push-pass"
serial=emulator-5574

command=$(python3 -c '
import json, sys
try:
    print(json.load(sys.stdin).get("tool_input", {}).get("command", ""))
except Exception:
    pass
')

# 0 = not a push to check, 1 = check it.
verdict=$(printf '%s' "$command" | python3 -c '
import re, sys

text = sys.stdin.read().replace("\\\n", " ")
push = re.search(r"(?:^|[\s;&|(`])git(?:\s+(?:-[Cc]\s+\S+|-\S+))*\s+push\b[^\n;&|]*", text)
if not push:
    print(0)
    sys.exit(0)
args = push.group(0)
skip = re.search(r"(?:^|\s)(?:--delete|-d|--dry-run|-n)(?:\s|$)", args) or re.search(r"\s:\S", args)
print(0 if skip else 1)
')
[ "$verdict" = 1 ] || exit 0

head=$(git -C "$root" rev-parse HEAD 2>/dev/null || true)
[ -n "$head" ] || exit 0
[ "$(cat "$marker" 2>/dev/null || true)" = "$head" ] && exit 0

# Files this push carries: against the upstream branch if there is one, else against origin/dev.
base=$(git -C "$root" rev-parse --verify -q '@{upstream}' 2>/dev/null ||
    git -C "$root" merge-base HEAD origin/dev 2>/dev/null || true)
if [ -n "$base" ]; then
    changed=$(git -C "$root" diff --name-only "$base" HEAD 2>/dev/null)
    if ! printf '%s\n' "$changed" |
        grep -qE '(\.kt|\.kts|\.gradle|detekt\.yml|\.editorconfig|libs\.versions\.toml|gradle\.properties)$'; then
        exit 0
    fi
fi

if [ -z "${DEVELOPER_DIR:-}" ] && [ -d /Applications/Xcode.app/Contents/Developer ]; then
    DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer
    export DEVELOPER_DIR
fi

trim() {
    sed "s#file://$root/##; s#$root/##" |
        grep -E '^e: |FAILED|What went wrong|Execution failed|violation|\.kt:[0-9]+|^> |coverage|Rule violated' |
        grep -v -e '^> Task ' -e '^> Run with' -e '^> Get more help' -e '^> Try' | awk '!seen[$0]++' | head -40
}

deny() { # $1 = step name, $2 = full output
    report=$(printf '%s\n' "$2" | trim)
    [ -n "$report" ] || report=$(printf '%s\n' "$2" | tail -30)
    {
        printf 'PUSH DENIED — %s failed.\n\n%s\n\n' "$1" "$report"
        printf 'Fix it and push again. Nothing was pushed.\n'
    } >&2
    exit 2
}

output=$(cd "$root" && ./gradlew --console=plain --continue jacocoCoverageVerification 2>&1) ||
    deny "the coverage floor check (jacocoCoverageVerification)" "$output"

output=$(cd "$root" && ./gradlew --console=plain --continue iosSimulatorArm64Test 2>&1) ||
    deny "the iOS simulator tests (iosSimulatorArm64Test)" "$output"

output=$("$root/scripts/emulator/verbeclair-emulator.sh" start phone 2>&1) ||
    deny "starting the VerbeClair emulator ($serial)" "$output"

output=$(cd "$root" && ANDROID_SERIAL=$serial ./gradlew --console=plain --continue connectedAndroidDeviceTest 2>&1) ||
    deny "the Android instrumented tests on $serial (AppKoinGraphTest is known to be flaky; rerun once if it is the only failure)" "$output"

mkdir -p "$state_dir" && printf '%s\n' "$head" >"$marker"
exit 0
