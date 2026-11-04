#!/bin/sh
# PreToolUse(Bash) hook: a PR to dev needs a /finalize pass on the current commit.
#
# /finalize writes the commit it passed on (READY_TO_PUSH) to .prism/verify/state/finalize-pass. Any
# newer commit changes HEAD, so the marker goes stale and /finalize has to run again.
#
# Reads the hook event (JSON) on stdin. Exit 2 with a message on stderr denies the command; exit 0 allows it.
# Only `gh pr create` is looked at, so every other Bash command passes through untouched.
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

# base=<ref> when the command is `gh pr create`, empty otherwise.
parsed=$(printf '%s' "$command" | python3 -c '
import os, re, shlex, sys

SHELLS = ("bash", "sh", "zsh", "dash")
# Words that can stand in front of the program without changing which program runs.
PREFIXES = ("{", "!", "time", "command", "exec", "env", "nohup")


def scan(text, depth=0):
    """Return the --base of a `gh pr create` found in text, "" when it has none, None when there is no such command."""
    # A backslash before a newline continues the command. Any other newline separates commands like `;`: shlex
    # treats it as whitespace, so without this a command on a later line would never be looked at. Inside quotes
    # the `;` stays part of the token.
    text = text.replace("\\\n", " ").replace("\n", " ; ")
    lexer = shlex.shlex(text, posix=True, punctuation_chars=True)
    lexer.whitespace_split = True
    try:
        tokens = list(lexer)
    except ValueError:
        # Unbalanced quotes (an apostrophe in a heredoc body is enough). Do not let that open the gate: fall back
        # to a text match and fail closed, so a missing --base denies. \x27 is a single quote.
        if not re.search(r"(?:^|[\s;&|({])(?:\S*/)?gh\s.*\bpr\s+create\b", text):
            return None
        found = re.findall(r"(?:^|\s)(?:--base(?:=|\s+)|-B\s*=?\s*)[\"\x27]?([^\s;&|()\"\x27]+)", text)
        return found[-1] if found else ""
    # Quoted text (a commit message, an echo) is one token, so only a real command can match. Split the
    # tokens into commands at the shell operators and look at the start of each.
    segments, current = [], []
    for tok in tokens:
        if tok and set(tok) <= set(";&|()"):
            segments.append(current)
            current = []
        else:
            current.append(tok)
    segments.append(current)
    for seg in segments:
        while seg and (re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*=.*", seg[0]) or seg[0] in PREFIXES):
            seg = seg[1:]
        if not seg:
            continue
        program = os.path.basename(seg[0])
        if program in SHELLS and "-c" in seg and depth < 3:
            i = seg.index("-c")
            inner = scan(seg[i + 1], depth + 1) if i + 1 < len(seg) else None
            if inner is not None:
                return inner
            continue
        if program != "gh" or len(seg) < 3:
            continue
        rest = [t for t in seg[1:] if not t.startswith("-")]
        # `gh -R owner/repo pr create` leaves the repo value ahead of "pr", so look for the pair, not the start.
        if not any(a == "pr" and b == "create" for a, b in zip(rest, rest[1:])):
            continue
        base = ""
        for i, tok in enumerate(seg):
            if tok in ("--base", "-B") and i + 1 < len(seg):
                base = seg[i + 1]
            elif tok.startswith("--base="):
                base = tok.split("=", 1)[1]
        return base
    return None


result = scan(sys.stdin.read())
if result is not None:
    print("base=" + result)
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
