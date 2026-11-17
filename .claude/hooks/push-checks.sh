#!/bin/sh
# PreToolUse(Bash) hook: before the agent's `git push`, run the JaCoCo coverage floors, the iOS simulator tests and
# the Android instrumented tests on the VerbeClair emulator, and deny the push when any of them fails. Cheapest first,
# so a failure never waits for the emulator to boot. The iOS tests are the simulator tests CI runs in its own job
# (commonTest and iosTest, Compose UI tests included); the project has no XCUITest suite.
#
# Reads the hook event (JSON) on stdin. Exit 2 with a trimmed failure report on stderr denies the command; exit 0
# allows it. Only commands that contain a `git push` are looked at (text that only mentions the command, such as a
# commit message, is also checked, and costs nothing once the HEAD has passed). Never active for a push the developer
# types in a terminal.
# The command is read as shell words (one linear pass: quotes, escapes and redirections are handled, nothing is
# matched as text), so a push is found however it is spelled: after `&&`/`;`/`|`, inside `bash -c "…"`, `sh -c '…'`,
# `eval` or `$( )` (a quoted word that mentions push is read again as a command), behind `sudo`/`timeout`/`env`,
# with a path, as `\git`, `"git"`, `gi""t` or `Git`, with a quoted subcommand, and with git's own options in front
# (`-C "/a b"`, `-c k="v w"`, `--git-dir=…`). The arguments of a push are the words of its own command: what follows
# a wrapper's closing quote (`bash -c "git push" -n`) and redirection targets (`> -n`) are not among them. Commands
# that never mention `push` are not parsed at all. A push quoted deeper than the parser reads (6 levels), or a
# command the parser cannot read, is checked instead of allowed.
# Known gaps, as for pr-gate.sh: a push built from a variable, an alias, a function or a script the command only
# runs; a bare `:` refspec (it pushes the matching branches but is read as a deletion), `--branches` and pattern
# refspecs (not denied like `--all`); the value of a push option that looks like a flag (`-o -n`); an error in the
# command extractor below, which allows the command. CI is the gate that cannot be skipped.
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

# The command is read as shell words (quotes and escapes removed), not matched as text: a regex over arbitrary text
# either misses wrapped forms or backtracks, and this runs on every Bash command. The tokenizer is one linear pass.
# A quoted word that mentions push is read again as a command (bash -c "...", eval "..."), to a fixed depth.
# Never write a single quote in this program: it sits inside a single-quoted shell string.
SQ = chr(39)
DQ = chr(34)
BS = chr(92)
NL = chr(10)
BT = chr(96)
MAX_DEPTH = 6
# Options git takes before the subcommand that consume the next word, even when it starts with a dash.
VALUE_OPTIONS = ("-C", "-c", "--git-dir", "--work-tree", "--namespace", "--super-prefix")
MOVES_HEAD = ("commit", "merge", "cherry-pick", "rebase", "revert", "am", "pull", "reset")
SWITCHES = ("switch", "checkout")
SEPARATORS = ";&|()" + NL + BT
# A run of ordinary characters inside a word, and the characters that end a stretch of double-quoted text.
PLAIN = re.compile("[^" + re.escape(SEPARATORS + "<>" + SQ + DQ + BS) + r"\s]+")
DOUBLE_STOP = re.compile("[" + re.escape(DQ + BS) + "]")

current = os.environ.get("CUR_BRANCH", "")
text = sys.stdin.read().replace(BS + NL, " ")
# Most commands never mention a push: skip all parsing, so a long unrelated command costs nothing. Quotes and
# backslashes are ignored in this test, because pu"sh and pu\sh are still the word push.
if "push" not in re.sub("[" + re.escape(BS + DQ + SQ) + "]", "", text):
    print(0)
    sys.exit(0)


def tokenize(source, depth):
    """Split source into commands: lists of words with quotes and escapes removed."""
    commands = []
    words = []
    word = []
    in_word = False
    quoted = False  # the word has a quoted part or an escape
    skip_next = False  # the next word is the target of a redirection, not an argument
    i = 0
    n = len(source)

    def end_word():
        nonlocal word, in_word, quoted, skip_next
        if not in_word:
            return
        value = "".join(word)
        if quoted and "push" in value:
            # A quoted string may itself be a command (bash -c "...", eval "..."): read what is inside it. Nested
            # deeper than the limit it is not read, so the caller treats the whole command as unreadable and checks.
            if depth >= MAX_DEPTH:
                raise ValueError("push nested in quotes deeper than " + str(MAX_DEPTH))
            commands.extend(tokenize(value, depth + 1))
        if skip_next:
            skip_next = False
        else:
            words.append(value)
        word = []
        in_word = False
        quoted = False

    def end_command():
        nonlocal words, skip_next
        end_word()
        if words:
            commands.append(words)
        words = []
        skip_next = False

    while i < n:
        ch = source[i]
        if ch == BS:
            if i + 1 < n:
                word.append(source[i + 1])
                in_word = True
                quoted = True
            i += 2
        elif ch == SQ:
            end = source.find(SQ, i + 1)
            if end < 0:
                end = n
            word.append(source[i + 1:end])
            in_word = True
            quoted = True
            i = end + 1
        elif ch == DQ:
            i += 1
            while i < n:
                stop = DOUBLE_STOP.search(source, i)
                if stop is None:
                    word.append(source[i:])
                    i = n
                    break
                word.append(source[i:stop.start()])
                i = stop.start()
                if source[i] == DQ:
                    i += 1
                    break
                # A backslash: only these characters are escaped inside double quotes.
                if i + 1 < n and source[i + 1] in (DQ, BS, "$", BT, NL):
                    word.append(source[i + 1])
                    i += 2
                else:
                    word.append(BS)
                    i += 1
            in_word = True
            quoted = True
        elif ch in SEPARATORS:
            end_command()
            i += 1
        elif ch in "<>":
            # Digits right before the operator (2>file) belong to the redirection, not to the command.
            if in_word and not quoted and "".join(word).isdigit():
                word = []
                in_word = False
            else:
                end_word()
            while i < n and source[i] in "<>":
                i += 1
            if i < n and source[i] == "&":
                i += 1
            skip_next = True
        elif ch.isspace():
            end_word()
            i += 1
        elif ch == "#" and not in_word:
            # A comment runs to the end of the line; the newline itself still ends the command.
            end = source.find(NL, i)
            i = n if end < 0 else end
        else:
            run = PLAIN.match(source, i)
            word.append(run.group())
            in_word = True
            i = run.end()
    end_command()
    return commands


def analyse(commands):
    """Return the argument lists of the pushes, and whether the command moves HEAD or switches branch."""
    pushes = []
    moves_head = False
    switches = False
    for words in commands:
        i = 0
        while i < len(words):
            # git may be a path (/usr/bin/git), be quoted or escaped (the words are already unquoted), or differ in
            # case (the default macOS file system ignores it).
            if words[i].rsplit("/", 1)[-1].lower() != "git":
                i += 1
                continue
            j = i + 1
            while j < len(words):
                if words[j] in VALUE_OPTIONS:
                    j += 2
                elif words[j].startswith("-"):
                    j += 1
                else:
                    break
            subcommand = words[j] if j < len(words) else ""
            if subcommand == "push":
                pushes.append(words[j + 1:])
            elif subcommand in MOVES_HEAD:
                moves_head = True
            elif subcommand in SWITCHES:
                switches = True
            i = j
    return pushes, moves_head, switches


verdict = 0
foreign = []
try:
    pushes, moves_head, switches = analyse(tokenize(text, 0))
    # Every push in the command is looked at: check it when ANY of them is a real push.
    for args in pushes:
        flags = [token for token in args if token.startswith("-")]
        if "--dry-run" in flags or "-n" in flags:
            continue
        # The first non-flag word is the remote; the rest are refspecs. Only a push whose every refspec deletes a
        # branch is skipped: git push origin feat :old still pushes feat.
        refspecs = [token for token in args if not token.startswith("-")][1:]
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
    # The hook runs BEFORE the command, so when the same command also moves HEAD (git commit ... && git push) the
    # HEAD read below is the old one. Verdict 2 = check it, and trust neither the passed-HEAD marker nor the
    # changed-files shortcut.
    if verdict and moves_head:
        verdict = 2
except Exception:
    # The text mentions a push and could not be read: check it, without the shortcuts, rather than allow it.
    verdict = 2
    foreign = []
    switches = False
print(verdict)
for ref in dict.fromkeys(foreign):
    print(ref)
if verdict and switches:
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
