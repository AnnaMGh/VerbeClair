#!/bin/sh
# PostToolUse(Edit|Write) hook: format the one Kotlin file the agent just edited with ktlint.
#
# Reads the hook event (JSON) on stdin. Only .kt and .kts files are looked at; every other file passes through.
# The file is formatted in place and the agent is not interrupted. A violation that formatting cannot fix (a wildcard
# import, say) is reported on stderr with exit 2, which Claude Code shows to the agent. Exit 0 in every other case,
# including when the ktlint tooling is not available: the hook never blocks an edit because it cannot run.
#
# ktlint runs directly from the classpath that `./gradlew ktlintToolingClasspath` writes to
# build/ktlint/cli-classpath.txt, because a Gradle invocation per edited file is too slow. It runs from the file's own
# module directory so the module's ktlint.baseline.xml applies exactly as it does in `ktlintCheck`.
set -u

root=${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel 2>/dev/null || pwd)}
classpath_file="$root/build/ktlint/cli-classpath.txt"

file=$(python3 -c '
import json, sys
try:
    print(json.load(sys.stdin).get("tool_input", {}).get("file_path", ""))
except Exception:
    pass
')

case "$file" in
*.kt | *.kts) ;;
*) exit 0 ;;
esac

[ -f "$file" ] || exit 0
[ -s "$classpath_file" ] || exit 0
command -v java >/dev/null 2>&1 || exit 0

# The module directory is the nearest parent holding a build script; fall back to the repository root.
module=$(dirname "$file")
while [ "$module" != "/" ] && [ "$module" != "$root" ]; do
    if [ -f "$module/build.gradle.kts" ]; then break; fi
    module=$(dirname "$module")
done
[ "$module" = "/" ] && module=$root

# The path relative to the module, so baseline entries (recorded relative to the module) match.
relative=${file#"$module"/}

baseline=""
[ -f "$module/ktlint.baseline.xml" ] && baseline="--baseline=ktlint.baseline.xml"

classpath=$(cat "$classpath_file")
# shellcheck disable=SC2086 # $baseline is deliberately empty or a single argument
output=$(cd "$module" && java -cp "$classpath" com.pinterest.ktlint.Main --format --relative --reporter=plain \
    $baseline "$relative" 2>&1)
status=$?

[ "$status" -eq 0 ] && exit 0

# Keep only the violation lines (path:line:col: message); drop ktlint's own logging.
violations=$(printf '%s\n' "$output" | grep -E '^[^ ]+:[0-9]+:[0-9]+: ' | awk '!seen[$0]++')
[ -z "$violations" ] && exit 0

{
    echo "ktlint formatted $relative but these violations cannot be fixed automatically:"
    printf '%s\n' "$violations"
} >&2
exit 2
