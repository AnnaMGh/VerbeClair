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
PREFIXES = ("{", "!", "time", "command", "exec", "env", "nohup", "sudo",
            "if", "then", "else", "elif", "while", "until", "do")


def scan(text, depth=0):
    """Return the --base of a `gh pr create` found in text, "" when it has none, None when there is no such command."""
    # A backslash before a newline continues the command. Any other newline separates commands like `;`: shlex
    # treats it as whitespace, so without this a command on a later line would never be looked at. Inside quotes
    # the `;` stays part of the token.
    # A heredoc body is data, not commands: drop it (keeping the line that opens it) so an apostrophe or a mention
    # of the gate in a commit message neither breaks the parse nor reads as a command. \x27 is a single quote.
    text = re.sub(
        r"(<<-?[ \t]*[\"\x27]?([^\s\"\x27<>;&|()]+)[\"\x27]?[^\n]*)\n.*?\n[ \t]*\2[ \t]*(?=\n|$)", r"\1", text, flags=re.S
    )
    text = text.replace("\\\n", " ").replace("\n", " ; ")
    lexer = shlex.shlex(text, posix=True, punctuation_chars=True)
    lexer.whitespace_split = True
    # shlex reads # as a comment up to the next newline, and every newline is now a ;, so a comment would hide
    # the rest of the command. Comments are not stripped: an extra token in a segment only makes the match stricter.
    lexer.commenters = ""
    try:
        tokens = list(lexer)
    except ValueError:
        # Unbalanced quotes (an apostrophe in a heredoc body is enough). Do not let that open the gate: fall back
        # to a text match and fail closed, so a missing --base denies. \x27 is a single quote.
        if not re.search(r"\bgh\s.*\bpr\s+(?:create|new)\b", text):
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
        # A command substitution in backticks leaves the backtick on the first token.
        seg = [t.lstrip("`") for t in seg[:1]] + seg[1:]
        # ... and `VAR=`gh ...` would be dropped as an assignment together with the command inside it.
        m = re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*=`+(.*)", seg[0]) if seg else None
        if m:
            seg = ([m.group(1)] if m.group(1) else []) + seg[1:]
        while seg and (re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*=.*", seg[0]) or seg[0] in PREFIXES or seg[0] == "timeout"):
            if seg[0] == "timeout":
                # `timeout [options] DURATION program ...`; -s and -k take a value of their own.
                seg = seg[1:]
                while seg and seg[0].startswith("-"):
                    seg = seg[2:] if seg[0] in ("-s", "-k", "--signal", "--kill-after") else seg[1:]
                if seg and re.fullmatch(r"[0-9.]+[smhd]?", seg[0]):
                    seg = seg[1:]
            else:
                seg = seg[1:]
        if not seg:
            continue
        program = os.path.basename(seg[0])
        # `-c`, or flags combined with it: -lc, -ec, -xc
        flags = [i for i, t in enumerate(seg[1:], 1) if re.fullmatch(r"-[A-Za-z]*c[A-Za-z]*", t)]
        if program in SHELLS and flags and depth < 3:
            i = flags[0]
            inner = scan(seg[i + 1], depth + 1) if i + 1 < len(seg) else None
            if inner is not None:
                return inner
            continue
        if program != "gh" or len(seg) < 3:
            continue
        if "--help" in seg or "-h" in seg:
            continue
        rest = [t for t in seg[1:] if not t.startswith("-")]
        # `gh -R owner/repo pr create` leaves the repo value ahead of "pr", so look for the pair, not the start.
        # `new` is the built-in alias of `create`.
        if not any(a == "pr" and b in ("create", "new") for a, b in zip(rest, rest[1:])):
            continue
        base = ""
        for i, tok in enumerate(seg):
            if tok in ("--base", "-B") and i + 1 < len(seg):
                base = seg[i + 1]
            elif tok.startswith("--base="):
                base = tok.split("=", 1)[1]
        return base.strip("`")
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
