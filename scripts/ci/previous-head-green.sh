#!/usr/bin/env bash
# Reads the JSON of GET /repos/{repo}/commits/{sha}/check-runs on stdin and prints "true" only if the two fast check
# jobs of that commit each concluded success or skipped (a skipped job passed its own safety check or was a draft that
# a later ready_for_review run covers). Anything else, including missing, running, cancelled or unreadable runs,
# prints "false". The heavy jobs aren't looked at: only updates of dev can be skipped, and they don't run them.
# A commit can have several runs of a job: the newest one that wasn't skipped counts. A draft pull request into main
# adds skipped runs to the head of dev, which must not hide a failed run of that commit.
#
# Usage: gh api "repos/$REPO/commits/$SHA/check-runs?per_page=100" | previous-head-green.sh

set -uo pipefail

result=$(jq -r '
  ["Checks and unit tests", "iOS simulator tests"] as $want
  | ((.check_runs // []) | group_by(.name)
     | map((map(select(.conclusion != "skipped")) | if length > 0 then . else null end) // . | max_by(.id))) as $latest
  | if all($want[]; . as $name | any($latest[]; .name == $name and (.conclusion == "success" or .conclusion == "skipped")))
    then "true" else "false" end
' 2>/dev/null)

# jq prints nothing for empty input, so default to false
echo "${result:-false}"
