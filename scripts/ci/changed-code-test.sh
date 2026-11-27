#!/usr/bin/env bash
# Checks the decisions of changed-code.sh, previous-head-green.sh and decide-jobs.sh. Run: bash scripts/ci/changed-code-test.sh
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
green() { # name expected json
  actual=$(printf '%s' "$3" | ./previous-head-green.sh)
  if [ "$actual" = "$2" ]; then echo "ok   $1"; else echo "FAIL $1: expected $2, got $actual"; fail=1; fi
}
run() { printf '{"id":%s,"name":"%s","conclusion":%s}' "$1" "$2" "$3"; }
C="Checks and unit tests"; I="iOS simulator tests"; A="Android device tests"
green "all success"            true  "{\"check_runs\":[$(run 1 "$C" '"success"'),$(run 2 "$I" '"success"')]}"
green "skipped passes"         true  "{\"check_runs\":[$(run 1 "$C" '"skipped"'),$(run 2 "$I" '"skipped"')]}"
green "one failure"            false "{\"check_runs\":[$(run 1 "$C" '"success"'),$(run 2 "$I" '"failure"')]}"
green "one cancelled"          false "{\"check_runs\":[$(run 1 "$C" '"cancelled"'),$(run 2 "$I" '"success"')]}"
green "one still running"      false "{\"check_runs\":[$(run 1 "$C" '"success"'),$(run 2 "$I" null)]}"
green "one job missing"        false "{\"check_runs\":[$(run 1 "$C" '"success"')]}"
green "rerun wins"             true  "{\"check_runs\":[$(run 1 "$C" '"failure"'),$(run 9 "$C" '"success"'),$(run 2 "$I" '"success"')]}"
green "later failure wins"     false "{\"check_runs\":[$(run 1 "$C" '"success"'),$(run 9 "$C" '"failure"'),$(run 2 "$I" '"success"')]}"
green "skip hides no failure"  false "{\"check_runs\":[$(run 1 "$C" '"failure"'),$(run 9 "$C" '"skipped"'),$(run 2 "$I" '"success"')]}"
green "skip hides no running"  false "{\"check_runs\":[$(run 1 "$C" '"success"'),$(run 2 "$I" null),$(run 9 "$I" '"skipped"')]}"
green "heavy jobs ignored"     true  "{\"check_runs\":[$(run 1 "$C" '"success"'),$(run 2 "$I" '"success"'),$(run 3 "$A" '"failure"')]}"
green "other check runs"       true  "{\"check_runs\":[$(run 5 "Changes" '"failure"'),$(run 1 "$C" '"success"'),$(run 2 "$I" '"success"')]}"
green "no check runs"     false '{"check_runs":[]}'
green "not json"          false 'garbage'
green "empty input"       false ''

# decide-jobs.sh: which jobs run. Expected: the checks, ios and heavy outputs, in that order.
jobs() { # name expected event action target draft code green
  local name=$1 expected=$2 actual
  shift 2
  actual=$(./decide-jobs.sh "$@" | cut -d= -f2 | paste -sd' ' -)
  if [ "$actual" = "$expected" ]; then echo "ok   $name"; else echo "FAIL $name: expected $expected, got $actual"; fail=1; fi
}
fast="true true false"; all="true true true"; after_merge="false true true"; none="false false false"
#    name                              expected        event             action           target draft code  green
jobs "PR into dev"                     "$fast"         pull_request      opened           dev    false true  false
jobs "PR into main"                    "$all"          pull_request      opened           main   false true  false
jobs "push to dev"                     "$fast"         push              ""               dev    ""    true  false
jobs "push to main"                    "$after_merge"  push              ""               main   ""    true  false
jobs "manual run"                      "$all"          workflow_dispatch ""               dev    ""    true  false
jobs "docs on PR into dev, green"      "$none"         pull_request      synchronize      dev    false false true
jobs "docs on push to dev, green"      "$none"         push              ""               dev    ""    false true
jobs "docs on PR into dev, not green"  "$fast"         pull_request      synchronize      dev    false false false
jobs "docs PR into dev just opened"    "$fast"         pull_request      opened           dev    false false true
jobs "docs PR into dev reopened"       "$fast"         pull_request      reopened         dev    false false true
jobs "docs draft marked ready"         "$fast"         pull_request      ready_for_review dev    false false true
jobs "docs on PR into main"            "$all"          pull_request      synchronize      main   false false true
jobs "docs on push to main"            "$after_merge"  push              ""               main   ""    false true
jobs "manual run is never skipped"     "$all"          workflow_dispatch ""               dev    ""    false true
jobs "draft PR into dev"               "$none"         pull_request      synchronize      dev    true  true  false
jobs "draft PR into main"              "$none"         pull_request      opened           main   true  true  false
jobs "unknown event runs everything"   "$all"          merge_group       ""               dev    ""    false true
if ./decide-jobs.sh push >/dev/null 2>&1; then echo "FAIL missing arguments pass"; fail=1; else echo "ok   missing arguments fail"; fi
exit $fail
