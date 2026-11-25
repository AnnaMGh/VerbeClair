#!/usr/bin/env bash
# Reads the JSON of GET /repos/{repo}/commits/{sha}/check-runs on stdin and prints "true" only if the three CI jobs
# of that commit each concluded success or skipped (a skipped job passed its own safety check or was a draft that
# a later ready_for_review run covers). Anything else, including missing, running, cancelled or unreadable runs,
# prints "false".
#
# When the event is "push" the commit's "iOS release link" job must also have passed. That job is skipped by every
# pull_request run on the same commit (and by a docs-only push), and a skipped run must not hide a failed one, so
# skipped runs of it are ignored and the latest remaining run has to be a success. A commit whose link runs were all
# skipped counts as not green, so the next push links the release frameworks again.
#
# Usage: gh api "repos/$REPO/commits/$SHA/check-runs?per_page=100" | previous-head-green.sh [event]

set -uo pipefail

event=${1:-}

result=$(jq -r --arg event "$event" '
  ["Checks and unit tests", "iOS simulator tests", "Android device tests"] as $want
  | ((.check_runs // []) | group_by(.name) | map(max_by(.id))) as $latest
  | ([(.check_runs // [])[] | select(.name == "iOS release link" and .conclusion != "skipped")] | max_by(.id)) as $link
  | if all($want[]; . as $name | any($latest[]; .name == $name and (.conclusion == "success" or .conclusion == "skipped")))
       and ($event != "push" or ($link != null and $link.conclusion == "success"))
    then "true" else "false" end
' 2>/dev/null)

# jq prints nothing for empty input, so default to false
echo "${result:-false}"
