#!/bin/sh
# PreToolUse(Bash) hook: a PR to dev needs a /finalize pass on the current commit.
#
# /finalize writes the commit it passed on (READY_TO_PUSH) to .prism/verify/state/finalize-pass. Any
# newer commit changes HEAD, so the marker goes stale and /finalize has to run again.
#
# Reads the hook event (JSON) on stdin. Exit 2 with a message on stderr denies the command; exit 0 allows it.
# Only commands whose text contains `gh pr create` / `gh pr new` are looked at; every other Bash command passes through.
set -u

root=${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel 2>/dev/null || pwd)}
marker="$root/.prism/verify/state/finalize-pass"

command=$(python3 -c '
import json, sys
try:
    print(json.load(sys.stdin).get("tool_input", {}).get("command", ""))
except Exception:
    pass
')

# Plain text match on the whole command, no shell parsing: quoting, wrappers (bash -c, timeout, if/then, backticks)
# and heredocs do not matter, the text either contains `gh ... pr create|new` or it does not. The price is a false
# positive when the text only mentions the command (a commit message, an echo); the denial says how to proceed.
# base=<ref> when the command is `gh pr create`, empty otherwise.
parsed=$(printf '%s' "$command" | python3 -c '
import re, sys

text = sys.stdin.read().replace("\\\n", " ")
if not re.search(r"\bgh\b[^\n]*\bpr\s+(?:create|new)\b", text):
    sys.exit(0)
found = re.findall(r"(?:^|\s)(?:--base(?:=|\s+)|-B\s*=?\s*)[\"\x27`]?([^\s;&|()\"\x27`]+)", text)
# Fail closed: only a literal branch name that is not dev is exempt. Any dev, a variable ($B), a substitution or
# origin/dev counts as dev, so a later "--base main" in a body cannot hide the real base.
def is_dev(base):
    return base.split("/")[-1] == "dev" or not re.fullmatch(r"[A-Za-z0-9._-]+(?:/[A-Za-z0-9._-]+)*", base)


print("base=" + ("dev" if any(is_dev(b) for b in found) else found[-1] if found else ""))
')

[ -z "$parsed" ] && exit 0
base=${parsed#base=}

if [ -z "$base" ]; then
    printf 'PR DENIED — pass --base explicitly.\n\n' >&2
    printf '`gh pr create` without --base targets the repository default branch (main). Work branches\n' >&2
    printf 'merge into dev, so use `--base dev`.\n' >&2
    exit 2
fi

[ "$base" = dev ] || exit 0

head=$(git -C "$root" rev-parse HEAD 2>/dev/null || true)
passed=$(cat "$marker" 2>/dev/null || true)

if [ -n "$head" ] && [ "$head" = "$passed" ]; then
    exit 0
fi

printf 'PR DENIED — /finalize has not passed on this commit.\n\n' >&2
printf 'HEAD:            %s\n' "${head:-unknown}" >&2
printf 'Last /finalize:  %s\n\n' "${passed:-none recorded}" >&2
printf 'Run /finalize (it ends in /verify) and wait for READY_TO_PUSH, then open the PR again.\n' >&2
printf 'Any new commit makes the pass stale, so run it again after changing anything.\n' >&2
exit 2
