#!/bin/sh
# PreToolUse(Bash) hook: before the agent's `git push`, run the JaCoCo coverage floors, the iOS simulator tests and
# the Android instrumented tests on the VerbeClair emulator, and deny the push when any of them fails. Cheapest first,
# so a failure never waits for the emulator to boot. The iOS tests are the simulator tests CI runs in its own job
# (commonTest and iosTest, Compose UI tests included); the project has no XCUITest suite.
#
# Reads the hook event (JSON) on stdin. Exit 2 with a trimmed failure report on stderr denies the command; exit 0
# allows it. Only commands whose text contains `git push` are looked at (a plain text match, like pr-gate.sh: text
# that only mentions the command is also checked, and costs nothing once the HEAD has passed). Never active for a
# push the developer types in a terminal.
# The match sees a push wherever `git` starts a word: after `&&`/`;`/`|`, inside `bash -c "…"`, `sh -c '…'` or `eval`,
# inside `$( )`, backticks or a subshell, behind `sudo`/`timeout`/`env`, with a path (`/usr/bin/git`), written `\git`
# or `"git"`, and with git's own options in front, whose values may be quoted (`-C "/a b"`, `-c k="v w"`,
# `--git-dir=…`). The arguments of a push inside a wrapper stop at the wrapper's closing quote, so a flag after it
# (`bash -c "git push" -n`) is not read as the push's own. Commands that never mention `push` are not parsed at all.
# Known gaps, as for pr-gate.sh: a push built from a variable, an alias or a quoted subcommand (`git "push"`), a
# quoted refspec inside a wrapper (its closing quote ends the arguments early), and a push made by a script the
# command only runs. CI is the gate that cannot be skipped.
#
# Skipped (exit 0): branch deletions and dry runs; a push whose commits change no Kotlin, Gradle, detekt or
# editorconfig file; a HEAD that already passed (recorded in .claude/state/push-pass, gitignored). Neither shortcut
# applies when the same command also commits (`git commit … && git push`): the hook sees the old HEAD, so it checks.
#
# Denied (exit 2) without running anything: a push of a branch other than the checked-out one when that branch carries
# Kotlin, Gradle, detekt or editorconfig changes (the checks run on the working tree, so they cannot vouch for another
# branch: check it out and push it), `--all`/`--mirror`, and a `git switch`/`git checkout` in the same command as the
# push (the hook sees the branch before the switch: run the switch as its own command first).
# A pass is recorded only when the working tree is clean, so it always describes the committed HEAD.
#
# The device tests run on emulator-5574 only (ANDROID_SERIAL), never on another project's emulator.
set -u

root=${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel 2>/dev/null || pwd)}
state_dir="$root/.claude/state"
marker="$state_dir/push-pass"
serial=emulator-5574

command=$(python3 -c '
import json, sys
try:
    print(json.load(sys.stdin).get("tool_input", {}).get("command", ""))
except Exception:
    pass
')

current=$(git -C "$root" symbolic-ref --short -q HEAD 2>/dev/null || true)

# Line 1: 0 = not a push to check, 1 = check it, 2 = check it without the shortcuts. Further lines: refs the push
# carries that are not the checked-out branch, and `!switch` when the command also switches branch.
verdict_out=$(printf '%s' "$command" | CUR_BRANCH="$current" python3 -c '
import os, re, sys

current = os.environ.get("CUR_BRANCH", "")
text = sys.stdin.read().replace("\\\n", " ")
if "push" not in text:
    # Most commands never mention a push: skip all matching, so a long unrelated command costs nothing.
    print(0)
    sys.exit(0)


def strip_comments(source):
    # A shell comment starts at an unquoted `#` that follows whitespace and runs to the end of the line. A `#` inside
    # quotes (a commit message such as "Closes #42") is text, not a comment.
    out = []
    quote = None
    in_comment = False
    previous = " "
    escaped = False
    for char in source:
        if escaped:
            # The character after a backslash is literal (outside single quotes), so an escaped quote opens nothing.
            escaped = False
            out.append(char)
            previous = char
            continue
        if char == chr(92) and quote != chr(39) and not in_comment:
            escaped = True
            out.append(char)
            previous = char
            continue
        if in_comment:
            if char == "\n":
                in_comment = False
                out.append(char)
            previous = char
            continue
        if quote:
            if char == quote:
                quote = None
        elif char in ("\"", chr(39)):
            quote = char
        elif char == "#" and previous.isspace():
            in_comment = True
            previous = char
            continue
        out.append(char)
        previous = char
    return "".join(out)


# A trailing shell comment is not part of the push arguments.
text = strip_comments(text)
DQ = chr(34)
SQ = chr(39)


def rx(pattern):
    # The program sits inside a single-quoted shell string, so a quote character is spelled with a placeholder.
    return pattern.replace("@D", DQ).replace("@S", SQ)


# A command that is itself inside a quoted string carries escaped quotes (`bash -c "git -C \"/a b\" push"`). Matching
# sees them as plain quotes, so the nested command reads like a top-level one.
match_text = text.replace(chr(92) + DQ, DQ).replace(chr(92) + SQ, SQ)
# One shell word: bare characters, backslash escapes and quoted parts, so a quoted path with a space stays one word.
# The alternatives start with different characters, so the match cannot backtrack exponentially on hostile input.
WORD = rx(r"(?:\\.|[^\s@D@S\\]|@D[^@D]*@D|@S[^@S]*@S)+")
# `git` starts a word after whitespace, `;&|`, a `(` or backtick (`$(git push)`), or a quote (`bash -c "git push"`,
# `eval "git push"`). It may be a path (`/usr/bin/git push`; every segment ends at a slash, so a run of text cannot be
# rescanned from each quote), be written `\git` to skip an alias, or be quoted itself (`"git" push`).
# Group q is the quote the word starts after (a wrapper), group c a closing quote right after `git`.
# Never write a single quote in this program: it sits inside a single-quoted shell string.
GIT = rx(r"(?:^|[\s;&|(`]|(?P<q>[@D@S]))(?:[^\s@D@S;&|(`/]*/)*\\?git(?P<c>[@D@S])?")
# Options git takes before the subcommand. A value may be quoted (`-C "/a b"`) or follow as its own word. Each option
# has exactly one parse (the generic flag branch excludes the options that take a value), so a failing match cannot
# backtrack exponentially.
GIT_OPTS = (
    r"(?:\s+(?:-[Cc]\s+@W"
    r"|--(?:git-dir|work-tree|namespace|super-prefix|config-env|exec-path)\s+@W"
    r"|-(?![Cc]\s)(?!-(?:git-dir|work-tree|namespace|super-prefix|config-env|exec-path)\s)@W))*"
).replace("@W", WORD)
# A subcommand name ends at whitespace, a quote, `)` or the end, but not at `-` (`git push-foo` is not `git push`).
END = r"(?![\w-])"
# The push arguments run to the end of the command part. When `git` follows a wrapper quote and has no closing quote of
# its own (`bash -c "git push origin x" -n`), they stop at that wrapper quote: what follows belongs to bash, not to git.
ARGS = r"(?P<args>(?(q)(?(c)[^\n;&|]*|(?:(?!(?P=q))[^\n;&|])*)|[^\n;&|]*))"
pushes = re.finditer(GIT + GIT_OPTS + r"\s+push" + END + ARGS, match_text)
# What a wrapper leaves stuck to a word: the closing quote of `bash -c "…"`, a `)`, a backtick, a stray backslash.
JUNK = DQ + SQ + "`()" + chr(92)
# Every push in the command is looked at: check it when ANY of them is a real push.
verdict = 0
foreign = []
for push in pushes:
    tokens = [token for token in (word.strip(JUNK) for word in push.group("args").split()) if token]
    flags = [token for token in tokens if token.startswith("-")]
    if "--dry-run" in flags or "-n" in flags:
        continue
    # The first non-flag word is the remote; the rest are refspecs. Only a push whose every refspec deletes a branch
    # is skipped: `git push origin feat :old` still pushes feat.
    refspecs = [token for token in tokens if not token.startswith("-")][1:]
    deletes = "--delete" in flags or "-d" in flags
    if refspecs and (deletes or all(token.startswith(":") for token in refspecs)):
        continue
    verdict = 1
    if "--all" in flags or "--mirror" in flags:
        foreign.append("--all")
    for token in refspecs:
        source = token.lstrip("+").split(":")[0]
        if source and source not in ("HEAD", current, "refs/heads/" + current):
            foreign.append(source)
# The hook runs BEFORE the command, so when the same command also moves HEAD (`git commit … && git push`) the HEAD read
# below is the old one. Verdict 2 = check it, and trust neither the passed-HEAD marker nor the changed-files shortcut.
moves_head = verdict and re.search(
    GIT + GIT_OPTS + r"\s+(?:commit|merge|cherry-pick|rebase|revert|am|pull|reset)" + END, match_text
)
switches = verdict and re.search(GIT + GIT_OPTS + r"\s+(?:switch|checkout)" + END, match_text)
if moves_head:
    verdict = 2
print(verdict)
for ref in dict.fromkeys(foreign):
    print(ref)
if switches:
    print("!switch")
')
verdict=$(printf '%s\n' "$verdict_out" | head -1)
[ "$verdict" = 1 ] || [ "$verdict" = 2 ] || exit 0

stop_push() { # $1 = reason
    printf 'PUSH DENIED — %s\n\nNothing was pushed.\n' "$1" >&2
    exit 2
}

pushed_refs=$(printf '%s\n' "$verdict_out" | tail -n +2)
if printf '%s\n' "$pushed_refs" | grep -qx '!switch'; then
    stop_push "the command switches branch and pushes. The checks would run on the branch before the switch, not on what is pushed. Run the switch as its own command, then push."
fi
if printf '%s\n' "$pushed_refs" | grep -qx -e '--all'; then
    stop_push "--all/--mirror pushes every branch, and the checks can only vouch for the checked-out one. Push the branches one at a time."
fi
# A branch other than the checked-out one: the checks run on the working tree, so they cannot vouch for it. Deny when it
# carries code changes, and say to check it out first. A ref git cannot resolve is left for git itself to reject.
for ref in $pushed_refs; do
    commit=$(git -C "$root" rev-parse --verify -q "$ref^{commit}" 2>/dev/null) || continue
    ref_base=$(git -C "$root" rev-parse --verify -q "$ref@{upstream}" 2>/dev/null ||
        git -C "$root" merge-base "$commit" origin/dev 2>/dev/null) || continue
    if git -C "$root" diff --name-only "$ref_base" "$commit" 2>/dev/null |
        grep -qE '(\.kt|\.kts|\.gradle|detekt\.yml|\.editorconfig|libs\.versions\.toml|gradle\.properties)$'; then
        stop_push "$ref is not the checked-out branch and carries Kotlin or Gradle changes. The checks run on the working tree, so they cannot vouch for it. Check it out (git switch $ref), then push."
    fi
done

head=$(git -C "$root" rev-parse HEAD 2>/dev/null || true)
[ -n "$head" ] || exit 0
base=
if [ "$verdict" = 1 ]; then
    [ "$(cat "$marker" 2>/dev/null || true)" = "$head" ] && exit 0

    # Files this push carries: against the upstream branch if there is one, else against origin/dev.
    base=$(git -C "$root" rev-parse --verify -q '@{upstream}' 2>/dev/null ||
        git -C "$root" merge-base HEAD origin/dev 2>/dev/null || true)
fi
if [ -n "$base" ]; then
    changed=$(git -C "$root" diff --name-only "$base" HEAD 2>/dev/null)
    if ! printf '%s\n' "$changed" |
        grep -qE '(\.kt|\.kts|\.gradle|detekt\.yml|\.editorconfig|libs\.versions\.toml|gradle\.properties)$'; then
        exit 0
    fi
fi

if [ -z "${DEVELOPER_DIR:-}" ] && [ -d /Applications/Xcode.app/Contents/Developer ]; then
    DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer
    export DEVELOPER_DIR
fi

trim() {
    sed "s#file://$root/##; s#$root/##" |
        grep -E '^e: |FAILED|What went wrong|Execution failed|violation|\.kt:[0-9]+|^> |coverage|Rule violated' |
        grep -v -e '^> Task ' -e '^> Run with' -e '^> Get more help' -e '^> Try' | awk '!seen[$0]++' | head -40
}

deny() { # $1 = step name, $2 = full output
    report=$(printf '%s\n' "$2" | trim)
    [ -n "$report" ] || report=$(printf '%s\n' "$2" | tail -30)
    {
        printf 'PUSH DENIED — %s failed.\n\n%s\n\n' "$1" "$report"
        printf 'Fix it and push again. Nothing was pushed.\n'
    } >&2
    exit 2
}

output=$(cd "$root" && ./gradlew --console=plain --continue jacocoCoverageVerification 2>&1) ||
    deny "the coverage floor check (jacocoCoverageVerification)" "$output"

output=$(cd "$root" && ./gradlew --console=plain --continue iosSimulatorArm64Test 2>&1) ||
    deny "the iOS simulator tests (iosSimulatorArm64Test)" "$output"

output=$("$root/scripts/emulator/verbeclair-emulator.sh" start phone 2>&1) ||
    deny "starting the VerbeClair emulator ($serial)" "$output"

output=$(cd "$root" && ANDROID_SERIAL=$serial ./gradlew --console=plain --continue connectedAndroidDeviceTest 2>&1) ||
    deny "the Android instrumented tests on $serial (AppKoinGraphTest is known to be flaky; rerun once if it is the only failure)" "$output"

# Remember the pass only when it covers exactly the commit: the checks ran on the working tree, so uncommitted changes
# (or a command that moves HEAD, verdict 2) mean HEAD itself was not what got tested.
if [ "$verdict" = 1 ] && [ -z "$(git -C "$root" status --porcelain 2>/dev/null)" ]; then
    mkdir -p "$state_dir" && printf '%s\n' "$head" >"$marker"
fi
exit 0
