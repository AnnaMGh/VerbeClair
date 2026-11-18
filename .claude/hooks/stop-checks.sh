#!/bin/sh
# Stop hook: when the agent stops after changing Kotlin, Gradle, detekt or editorconfig files, run detekt, ktlint, the
# Konsist tests and the JVM and Android host unit tests (not the iOS simulator tests), and send the agent back to fix
# any failure.
#
# Reads the hook event (JSON) on stdin. Exit 2 with a trimmed failure report on stderr blocks the stop; exit 0 allows it.
#
# Loop guard: the stop is blocked at most three times in a row for one turn. `stop_hook_active` is false on the first
# stop of a turn and true on the stops that follow a block, so a false value starts a fresh count. The counter lives in
# .claude/state/ (gitignored), one file per session. The fourth stop is allowed and the remaining failure is reported.
#
# "Changed" means: uncommitted changes, plus commits made since the last stop of this session that was clean (the HEAD
# recorded then). The recorded HEAD advances only when the checks passed or nothing relevant changed, so a failure in
# committed work is never forgotten after one block. On the first stop of a session there is no recorded HEAD, so the
# commits since the merge-base with origin/dev (or dev) count as changed.
set -u

root=${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel 2>/dev/null || pwd)}
state_dir="$root/.claude/state"
max_blocks=3

event=$(cat)
read_field() {
    printf '%s' "$event" | python3 -c '
import json, sys
try:
    value = json.load(sys.stdin).get(sys.argv[1], "")
    print(str(value).lower() if isinstance(value, bool) else value)
except Exception:
    pass
' "$1"
}
session=$(read_field session_id)
active=$(read_field stop_hook_active)
[ -n "$session" ] || session=default
session=$(printf '%s' "$session" | tr -c 'A-Za-z0-9_-' '_')

mkdir -p "$state_dir" 2>/dev/null || exit 0
counter="$state_dir/stop-blocks-$session"
last_head="$state_dir/stop-head-$session"

# A stop that is not a continuation after a block starts a new turn.
[ "$active" = true ] || rm -f "$counter"

head=$(git -C "$root" rev-parse HEAD 2>/dev/null || true)
previous=$(cat "$last_head" 2>/dev/null || true)
if [ -z "$previous" ]; then
    previous=$(git -C "$root" merge-base HEAD origin/dev 2>/dev/null || git -C "$root" merge-base HEAD dev 2>/dev/null || true)
fi
record_head() { [ -n "$head" ] && printf '%s\n' "$head" >"$last_head"; }

changed=$(
    {
        # Not `status --porcelain`: it wraps a path with spaces or non-ASCII characters in quotes (`"dir \303\251/B.kt"`),
        # and the quote hides the extension from the filter below. These commands print the real path.
        git -C "$root" -c core.quotePath=false diff --name-only HEAD 2>/dev/null
        git -C "$root" -c core.quotePath=false ls-files --others --exclude-standard 2>/dev/null
        if [ -n "$previous" ] && [ "$previous" != "$head" ]; then
            git -C "$root" -c core.quotePath=false diff --name-only "$previous" "$head" 2>/dev/null
        fi
    } | sort -u
)

relevant=$(printf '%s\n' "$changed" | grep -E '(\.kt|\.kts|\.gradle|detekt\.yml|\.editorconfig|libs\.versions\.toml|gradle\.properties)$')
if [ -z "$relevant" ]; then
    rm -f "$counter"
    record_head
    exit 0
fi

# The simulator is chosen by DEVELOPER_DIR, set per command and never through xcode-select.
if [ -z "${DEVELOPER_DIR:-}" ] && [ -d /Applications/Xcode.app/Contents/Developer ]; then
    DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer
    export DEVELOPER_DIR
fi

# Cheapest first: static analysis and architecture tests, then the host unit tests (jvmTest, testAndroidHostTest).
# Not `check`/`allTests`: they also build and run the iOS simulator tests, which push-checks.sh runs before the push.
output=$(cd "$root" && ./gradlew --console=plain --continue detekt ktlintCheck :tooling:konsist:test :tooling:detekt-rules:test jvmTest testAndroidHostTest 2>&1)
status=$?

if [ "$status" -eq 0 ]; then
    rm -f "$counter"
    record_head
    exit 0
fi

blocks=$(cat "$counter" 2>/dev/null || echo 0)
blocks=$((blocks + 1))

# Trimmed failure report: the lines that name a failure, falling back to the tail of the output.
report=$(printf '%s\n' "$output" | sed "s#file://$root/##; s#$root/##" | grep -E '^e: |FAILED|What went wrong|Execution failed|violation|\.kt:[0-9]+|^> ' |
    grep -v -e '^> Task ' -e '^> Run with' -e '^> Get more help' -e '^> Try' | awk '!seen[$0]++' | head -40)
[ -n "$report" ] || report=$(printf '%s\n' "$output" | tail -30)

if [ "$blocks" -gt "$max_blocks" ]; then
    rm -f "$counter"
    printf 'Stop checks still fail after %s blocked stops; allowing the stop. Remaining failure:\n%s' \
        "$max_blocks" "$report" | python3 -c 'import json, sys; print(json.dumps({"systemMessage": sys.stdin.read()}))'
    exit 0
fi

printf '%s\n' "$blocks" >"$counter"
{
    printf 'Stop checks failed (block %s of %s). Fix these before finishing:\n\n%s\n\n' "$blocks" "$max_blocks" "$report"
    printf 'Re-run: ./gradlew --continue detekt ktlintCheck :tooling:konsist:test :tooling:detekt-rules:test jvmTest testAndroidHostTest\n'
    printf 'Formatting-only violations: ./gradlew ktlintFormat\n'
} >&2
exit 2
