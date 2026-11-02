#!/usr/bin/env bash
# Decides whether a pull request or push changes anything the heavy CI jobs can check.
# Prints "code=true" or "code=false" (append the output to $GITHUB_OUTPUT).
#
# Only documentation and agent configuration count as "not code". Anything else, and any doubt
# (no base to diff against, a git error), counts as code, so a mistake costs minutes, never coverage.
#
# Usage: changed-code.sh <base-ref> [head-ref]    (head-ref defaults to HEAD)
#        changed-code.sh --stdin                  (read the changed paths from stdin, for tests)

set -uo pipefail

is_code() {
  case "$1" in
    openspec/*|.claude/*|docs/*|*.md|LICENSE) return 1 ;;
    *) return 0 ;;
  esac
}

if [ "${1:-}" = "--stdin" ]; then
  files=$(cat)
else
  base="${1:-}"
  head="${2:-HEAD}"
  if [ -z "$base" ] || [ "${base//0/}" = "" ] || ! files=$(git diff --name-only "$base" "$head" 2>/dev/null); then
    echo "code=true"
    exit 0
  fi
fi

if [ -z "$files" ]; then
  echo "code=true"
  exit 0
fi

while IFS= read -r file; do
  if is_code "$file"; then
    echo "code=true"
    exit 0
  fi
done <<< "$files"

echo "code=false"
