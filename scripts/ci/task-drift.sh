#!/usr/bin/env bash
# The drift guard of the fast checks (Notion N-18). CI runs an explicit task list instead of ./gradlew build, so a
# new module or plugin could add a check that CI never runs. This fails, naming the task, when ./gradlew build has a
# test, detekt, ktlint or lint task that running the list wouldn't run and that isn't excluded below.
#
# Both sides are dry runs, so unqualified names in the list expand to every module, and nothing is executed.
#
# Usage: task-drift.sh [task-list]                              (task-list defaults to fast-tasks.txt)
#        task-drift.sh --print [task-list]                    (the listed tasks, one per line; the CI job runs them)
#        task-drift.sh --compare <build-dry> <list-dry> <task-list>   (saved dry-run outputs, for tests)

set -euo pipefail

here=$(cd "$(dirname "$0")" && pwd)
root=$(cd "$here/../.." && pwd)

# Does this task name (without the module path) look like a check? Deliberately broad: a test task of a new target
# (say desktopTest) has to fail here until it's listed or excluded. Only the helper tasks that prepare, compile or
# package something for a check are left out by their first word.
is_check() {
  case "$1" in
    assemble*|bundle*|check[A-Z]*|compile*|convert*|copy*|generate*|javaPreCompile*|link*|map*|merge*) return 1 ;;
    package*|pre[A-Z]*|prepare*|process*|*Classes|ktlintToolingClasspath) return 1 ;;
    test|test[A-Z]*|*Test|*Tests|detekt*|ktlint*|lint*) return 0 ;;
    *) return 1 ;;
  esac
}

# The checks the fast tier leaves out on purpose.
is_excluded() { # task path
  # Aggregates of one module, which only group tasks that are checked here one by one.
  case "$1" in
    :androidApp:test|:androidApp:lint|:androidApp:detektMain) return 0 ;;
  esac
  case "${1##*:}" in
    allTests|testAndroid) return 0 ;;
    # Release variants belong to the heavy tier.
    *Release*) return 0 ;;
    # iOS tests run in the "iOS simulator tests" job.
    iosSimulatorArm64Test) return 0 ;;
    # Compose resource tasks of the iOS test source sets, whose names end in "Test".
    *ForAppleTest|*ForNativeTest|*ForIos*Test) return 0 ;;
    *) return 1 ;;
  esac
}

# The task paths of a --dry-run output. Other lines (warnings, logs) are ignored.
tasks_of() {
  awk '$2 == "SKIPPED" && $1 ~ /^:/ { print $1 }' "$1" | LC_ALL=C sort -u
}

fail() {
  echo "task-drift: $*" >&2
  exit 1
}

listed_names() {
  sed -e 's/[[:space:]]*$//' -e 's/^[[:space:]]*//' "$1" | grep -vE '^(#|$)'
}

compare() { # build-dry list-dry task-list
  local build list missing='' checked=0 task
  build=$(tasks_of "$1")
  list=$(tasks_of "$2")
  [ -n "$build" ] || fail "no tasks in the dry run of ./gradlew build"
  [ -n "$list" ] || fail "no tasks in the dry run of the task list"

  while IFS= read -r task; do
    is_check "${task##*:}" || continue
    is_excluded "$task" && continue
    checked=$((checked + 1))
    grep -qxF -- "$task" <<< "$list" || missing="$missing  $task"$'\n'
  done <<< "$build"
  [ "$checked" -gt 0 ] || fail "found no check tasks in ./gradlew build"

  # Entries the list has to contain as written, because the comparison above can't notice when they're gone:
  # ./gradlew build doesn't compile the instrumented tests (listed without a module path, so that a new module is
  # covered too), and assembling the debug app isn't a check task.
  local names
  names=$(listed_names "$3") || fail "no tasks listed in $3"
  for task in compileAndroidDeviceTest :androidApp:assembleDebug; do
    grep -qxF -- "$task" <<< "$names" || missing="$missing  $task (required in the task list)"$'\n'
  done
  grep -q ':compileAndroidDeviceTest$' <<< "$list" \
    || missing="$missing  compileAndroidDeviceTest (no module runs it)"$'\n'

  if [ -n "$missing" ]; then
    echo "task-drift: the fast checks don't run these tasks:" >&2
    printf '%s' "$missing" >&2
    echo "Add them to scripts/ci/fast-tasks.txt, or add a named exclusion to scripts/ci/task-drift.sh." >&2
    exit 1
  fi
  echo "task-drift: ok, $checked check tasks of ./gradlew build are covered"
}

dry_run() { # output-file gradle-args...
  local out=$1
  shift
  if ! "$root/gradlew" -p "$root" "$@" --dry-run --quiet --console=plain > "$out" 2>&1; then
    cat "$out" >&2
    fail "the Gradle dry run failed"
  fi
}

if [ "${1:-}" = "--compare" ]; then
  [ $# -eq 4 ] || fail "--compare needs <build-dry> <list-dry> <task-list>"
  compare "$2" "$3" "$4"
  exit 0
fi

if [ "${1:-}" = "--print" ]; then
  listed_names "${2:-$here/fast-tasks.txt}" || fail "no tasks listed in ${2:-$here/fast-tasks.txt}"
  exit 0
fi

task_list=${1:-$here/fast-tasks.txt}
names=$(listed_names "$task_list") || fail "no tasks listed in $task_list"

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

args=()
while IFS= read -r name; do args+=("$name"); done <<< "$names"

dry_run "$tmp/build" build
dry_run "$tmp/list" "${args[@]}"
compare "$tmp/build" "$tmp/list" "$task_list"
