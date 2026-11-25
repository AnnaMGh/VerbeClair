#!/usr/bin/env bash
# Checks the decisions of changed-code.sh and previous-head-green.sh. Run: bash scripts/ci/changed-code-test.sh
set -u
cd "$(dirname "$0")"
fail=0
check() { # name expected input
  actual=$(printf '%s' "$3" | ./changed-code.sh --stdin)
  if [ "$actual" = "code=$2" ]; then echo "ok   $1"; else echo "FAIL $1: expected code=$2, got $actual"; fail=1; fi
}
check "docs only"            false $'openspec/changes/x/tasks.md\nREADME.md\n.claude/settings.json'
check "kotlin"               true  $'shared/src/commonMain/Foo.kt'
check "mixed"                true  $'README.md\nshared/build.gradle.kts'
check "workflow"             true  '.github/workflows/ci.yml'
check "ci script"            true  'scripts/ci/run-device-tests.sh'
check "version catalog"      true  'gradle/libs.versions.toml'
check "nested markdown"      false 'core/designsystem/README.md'
check "empty diff"           true  ''
# without --stdin: no base, all-zero base (new branch) and an unknown ref fail safe to code=true
for base in "" "0000000000000000000000000000000000000000" "no-such-ref"; do
  actual=$(./changed-code.sh "$base")
  if [ "$actual" = "code=true" ]; then echo "ok   base '$base'"; else echo "FAIL base '$base': $actual"; fail=1; fi
done

# previous-head-green.sh: the check runs of the previous head, as the GitHub API returns them
green() { # name expected json [event]
  actual=$(printf '%s' "$3" | ./previous-head-green.sh "${4:-}")
  if [ "$actual" = "$2" ]; then echo "ok   $1"; else echo "FAIL $1: expected $2, got $actual"; fail=1; fi
}
run() { printf '{"id":%s,"name":"%s","conclusion":%s}' "$1" "$2" "$3"; }
C="Checks and unit tests"; I="iOS simulator tests"; A="Android device tests"; R="iOS release link"
green "all success"       true  "{\"check_runs\":[$(run 1 "$C" '"success"'),$(run 2 "$I" '"success"'),$(run 3 "$A" '"success"')]}"
green "skipped passes"    true  "{\"check_runs\":[$(run 1 "$C" '"skipped"'),$(run 2 "$I" '"skipped"'),$(run 3 "$A" '"skipped"')]}"
green "one failure"       false "{\"check_runs\":[$(run 1 "$C" '"success"'),$(run 2 "$I" '"failure"'),$(run 3 "$A" '"success"')]}"
green "one cancelled"     false "{\"check_runs\":[$(run 1 "$C" '"success"'),$(run 2 "$I" '"success"'),$(run 3 "$A" '"cancelled"')]}"
green "one still running" false "{\"check_runs\":[$(run 1 "$C" '"success"'),$(run 2 "$I" '"success"'),$(run 3 "$A" null)]}"
green "one job missing"   false "{\"check_runs\":[$(run 1 "$C" '"success"'),$(run 2 "$I" '"success"')]}"
green "rerun wins"        true  "{\"check_runs\":[$(run 1 "$C" '"failure"'),$(run 9 "$C" '"success"'),$(run 2 "$I" '"success"'),$(run 3 "$A" '"success"')]}"
green "other check runs"  true  "{\"check_runs\":[$(run 5 "Changes" '"failure"'),$(run 1 "$C" '"success"'),$(run 2 "$I" '"success"'),$(run 3 "$A" '"success"')]}"
green "no check runs"     false '{"check_runs":[]}'
green "not json"          false 'garbage'
green "empty input"       false ''
# on a push the iOS release link job counts too; a pull request has none
ALL3="$(run 1 "$C" '"success"'),$(run 2 "$I" '"success"'),$(run 3 "$A" '"success"')"
green "push, link success"   true  "{\"check_runs\":[$ALL3,$(run 4 "$R" '"success"')]}" push
green "push, link skipped"   true  "{\"check_runs\":[$ALL3,$(run 4 "$R" '"skipped"')]}" push
green "push, link failed"    false "{\"check_runs\":[$ALL3,$(run 4 "$R" '"failure"')]}" push
green "push, link running"   false "{\"check_runs\":[$ALL3,$(run 4 "$R" null)]}" push
green "push, link missing"   false "{\"check_runs\":[$ALL3]}" push
green "pr, link missing"     true  "{\"check_runs\":[$ALL3]}" pull_request
exit $fail
