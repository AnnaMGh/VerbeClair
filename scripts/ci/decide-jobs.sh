#!/usr/bin/env bash
# Decides which CI check jobs run (Notion N-18). Prints "checks=", "ios=" and "heavy=" lines, each true or false
# (append the output to $GITHUB_OUTPUT). "checks" is the Linux job, "ios" the iOS simulator tests, and "heavy" the
# Android release build, the Android device tests and the iOS frameworks.
#
# Usage: decide-jobs.sh <event> <action> <target-branch> <draft> <code> <previous-head-green>
#   target-branch is the base branch of a pull request, or the branch that was pushed or run by hand. code is the
#   result of changed-code.sh. Only "false" for code together with "true" for green can skip a run.

set -uo pipefail

if [ $# -ne 6 ]; then
  echo "decide-jobs: expected 6 arguments, got $#" >&2
  exit 1
fi
event=$1 action=$2 target=$3 draft=$4 code=$5 green=$6

# An event or branch that isn't listed below runs everything, so a mistake costs minutes, never coverage.
checks=true ios=true heavy=true
case "$event:$target" in
  pull_request:dev|push:dev) heavy=false ;;
  push:main) checks=false ;;
esac

# Only an update of dev, or of a pull request into it, can be skipped: documentation only, on top of a head that
# passed. Everything about main always runs, so a skipped heavy job can't hide an earlier failure.
skip=false
if [ "$target" = dev ] && [ "$code" = false ] && [ "$green" = true ]; then
  if [ "$event" = push ] || { [ "$event" = pull_request ] && [ "$action" = synchronize ]; }; then skip=true; fi
fi

if [ "$skip" = true ] || [ "$draft" = true ]; then checks=false ios=false heavy=false; fi

printf 'checks=%s\nios=%s\nheavy=%s\n' "$checks" "$ios" "$heavy"
