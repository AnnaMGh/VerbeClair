#!/usr/bin/env python3
"""Decisions of push-checks.sh. Run: python3 .claude/hooks/push-checks-test.py

Each case feeds the hook a Bash command as the hook event JSON, in a throwaway git repository whose `gradlew` and
emulator script are stubs that log their calls, and checks the exit code (2 = denied, 0 = allowed) and which stub
commands ran.
"""
import json
import os
import shlex
import stat
import subprocess
import sys
import tempfile
import time

HOOK = os.path.join(os.path.dirname(os.path.abspath(__file__)), "push-checks.sh")

GRADLEW = """#!/bin/sh
echo "gradlew ANDROID_SERIAL=${ANDROID_SERIAL:-none} $*" >> "$(dirname "$0")/calls.log"
if [ -f "$(dirname "$0")/fail" ] && echo "$*" | grep -q "$(cat "$(dirname "$0")/fail")"; then
    echo "> Task :core:domain:$(cat "$(dirname "$0")/fail") FAILED"
    # The detail a real run prints: the JaCoCo rule that failed, or the name of the failing test.
    if [ "$(cat "$(dirname "$0")/fail")" = jacocoCoverageVerification ]; then
        echo "Rule violated for bundle core:domain: instructions covered ratio is 0.50, but expected minimum is 0.80"
    else
        echo "FooTest > bar FAILED"
    fi
    echo "Execution failed for task ':core:domain:$(cat "$(dirname "$0")/fail")'."
    exit 1
fi
exit 0
"""
EMULATOR = """#!/bin/sh
echo "emulator $*" >> "$(dirname "$0")/../../calls.log"
exit 0
"""


def git(root, *args):
    return subprocess.run(["git", "-C", root, "-c", "user.name=t", "-c", "user.email=t@t", *args],
                          text=True, capture_output=True, check=True).stdout.strip()


def make_repo(root, changed_file):
    git(root, "init", "-q")
    with open(os.path.join(root, "README.md"), "w") as f:
        f.write("x\n")
    # Like the real repo, ignore what the checks and the hook write, so only a real edit makes the tree dirty.
    with open(os.path.join(root, ".gitignore"), "w") as f:
        f.write("calls.log\nfail\n.claude/state/\n")
    for path, body in (("gradlew", GRADLEW), ("scripts/emulator/verbeclair-emulator.sh", EMULATOR)):
        full = os.path.join(root, path)
        os.makedirs(os.path.dirname(full), exist_ok=True)
        with open(full, "w") as f:
            f.write(body)
        os.chmod(full, os.stat(full).st_mode | stat.S_IEXEC)
    git(root, "add", "-A")
    git(root, "commit", "-q", "-m", "base")
    git(root, "update-ref", "refs/remotes/origin/dev", "HEAD")
    full = os.path.join(root, changed_file)
    os.makedirs(os.path.dirname(full) or root, exist_ok=True)
    with open(full, "w") as f:
        f.write("changed\n")
    git(root, "add", "-A")
    git(root, "commit", "-q", "-m", "work")


def run(command, changed_file="shared/src/commonMain/kotlin/A.kt", fail=None, twice=False, prior_pass=False,
        upstream_at_head=False, dirty=False, foreign=None):
    with tempfile.TemporaryDirectory() as root:
        make_repo(root, changed_file)
        if foreign:
            # Another branch `feat`, one commit past origin/dev, while the checked-out branch keeps its own change.
            git(root, "switch", "-q", "-c", "feat", "origin/dev")
            with open(os.path.join(root, "F.kt" if foreign == "kt" else "F.md"), "w") as f:
                f.write("feat\n")
            git(root, "add", "-A")
            git(root, "commit", "-q", "-m", "feat work")
            git(root, "switch", "-q", "-")
        if upstream_at_head:
            # The branch is already pushed: its upstream is HEAD, so `git diff @{upstream} HEAD` is empty.
            git(root, "branch", "pushed", "HEAD")
            git(root, "branch", "--set-upstream-to=pushed")
        if dirty:
            # An uncommitted Kotlin edit that the command may be about to commit.
            with open(os.path.join(root, "B.kt"), "w") as f:
                f.write("uncommitted\n")
        # calls.log and the stubs' fail switch live outside git's view of the change.
        if fail:
            with open(os.path.join(root, "fail"), "w") as f:
                f.write(fail)
        if prior_pass:
            os.makedirs(os.path.join(root, ".claude", "state"))
            with open(os.path.join(root, ".claude", "state", "push-pass"), "w") as f:
                f.write(git(root, "rev-parse", "HEAD") + "\n")
        event = json.dumps({"tool_input": {"command": command}})
        env = {**os.environ, "CLAUDE_PROJECT_DIR": root}
        code = 0
        report = ""
        started = time.monotonic()
        for _ in range(2 if twice else 1):
            try:
                result = subprocess.run(["sh", HOOK], input=event, text=True, capture_output=True, env=env, timeout=30)
                code, report = result.returncode, result.stderr
            except subprocess.TimeoutExpired:
                # A hook that hangs on a hostile command stalls the agent: report it as a failed case (-1).
                code = -1
        elapsed = (time.monotonic() - started) / (2 if twice else 1)
        log_path = os.path.join(root, "calls.log")
        calls = open(log_path).read().splitlines() if os.path.exists(log_path) else []
        return code, calls, elapsed, report


def nested(command, depth):
    """The command inside `bash -c` quoted `depth` times, as the shell needs the nested quotes."""
    for _ in range(depth):
        command = "bash -c " + shlex.quote(command)
    return command


PUSH = "git push -u origin chore/x"
MAX_SECONDS = 4
GIT_WORD = "g" + "it"
CASES = [
    # name, command, kwargs, want exit, want number of stub calls, text every call line must contain
    ("push with Kotlin changes, all pass", PUSH, {}, 0, 4, None),
    ("push, coverage floor fails", PUSH, {"fail": "jacocoCoverageVerification"}, 2, 1, None),
    ("push, iOS simulator tests fail before the emulator boots", PUSH, {"fail": "iosSimulatorArm64Test"}, 2, 2, None),
    ("push, device tests fail", PUSH, {"fail": "connectedAndroidDeviceTest"}, 2, 4, None),
    ("push, other project's emulator never targeted", PUSH, {}, 0, 4, "ANDROID_SERIAL=emulator-5574|emulator start phone|jacocoCoverageVerification|iosSimulatorArm64Test"),
    ("push after &&", "git add . && git commit -m x && git push", {}, 0, 4, None),
    ("push with -C", "git -C /tmp/x push origin main", {}, 0, 4, None),
    ("push with a Gradle-only change", PUSH, {"changed_file": "build.gradle.kts"}, 0, 4, None),
    ("push with only a Markdown change", PUSH, {"changed_file": "docs/notes.md"}, 0, 0, None),
    ("branch delete", "git push origin --delete old", {}, 0, 0, None),
    ("branch delete by colon refspec", "git push origin :old", {}, 0, 0, None),
    ("dry run", "git push --dry-run", {}, 0, 0, None),
    ("dry run, then a real push", "git push --dry-run && git push -u origin feat", {}, 0, 4, None),
    ("branch delete, then a real push", "git push origin :old; git push origin feat", {}, 0, 4, None),
    ("dry run named only in a trailing comment", "git push origin feat # --dry-run", {}, 0, 4, None),
    ("quoted # in a commit message, then a real push", 'git commit -m "Closes #42" && git push origin x', {}, 0, 4, None),
    ("single-quoted # before a real push", "git commit -m 'wip #3' ; git push -u origin feat", {}, 0, 4, None),
    ("push that also deletes a branch", "git push origin feat :old", {}, 0, 4, None),
    ("escaped quote then # before a real push", 'git commit -m "a \\" b #1" && git push origin feat', {}, 0, 4, None),
    ("delete flag with two branches", "git push -d origin old1 old2", {}, 0, 0, None),
    ("same HEAD already passed", PUSH, {"prior_pass": True}, 0, 0, None),
    ("second push of the same HEAD runs the checks once", PUSH, {"twice": True}, 0, 4, None),
    # The hook runs before the command, so a commit in the same command is not in HEAD yet.
    ("commit and push, upstream already at HEAD, new Kotlin is checked",
     "git add -A && git commit -m x && git push", {"upstream_at_head": True, "dirty": True}, 0, 4, None),
    ("commit and push, a passed HEAD does not skip the checks",
     "git commit -am x && git push", {"upstream_at_head": True, "dirty": True, "prior_pass": True}, 0, 4, None),
    ("commit and push records no pass, so the next push checks again",
     "git commit -am x && git push", {"upstream_at_head": True, "dirty": True, "twice": True}, 0, 8, None),
    ("plain push, upstream at HEAD, nothing new: skipped", PUSH, {"upstream_at_head": True}, 0, 0, None),
    ("push with uncommitted edits is checked but records no pass", PUSH, {"dirty": True, "twice": True}, 0, 8, None),
    ("push on a clean tree records the pass", PUSH, {"twice": True}, 0, 4, None),
    # The checks run on the working tree, so a branch other than the checked-out one is judged by its own files.
    ("push of another branch with Kotlin, from a docs-only HEAD: denied",
     "git push origin feat", {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    ("push of another branch with Kotlin, with a refspec and a force plus",
     "git push origin +feat:feat", {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    ("push of another branch with Kotlin, from a Kotlin HEAD: still denied",
     "git push origin feat", {"foreign": "kt"}, 2, 0, None),
    ("push of another branch with only Markdown: allowed", "git push origin feat",
     {"changed_file": "docs/notes.md", "foreign": "md"}, 0, 0, None),
    ("push of HEAD by name is the checked-out branch", "git push origin HEAD", {}, 0, 4, None),
    ("switch to a branch and push in one command: denied",
     "git switch feat && git push", {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    ("checkout of a branch and push in one command: denied",
     "git checkout feat && git push", {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    ("push --all: denied", "git push --all origin", {}, 2, 0, None),
    ("absolute path to git is a push too", "/usr/bin/git push -u origin chore/x", {}, 0, 4, None),
    ("command git push is a push too", "command git push -u origin chore/x", {}, 0, 4, None),
    # A push wrapped in a quoted string (`bash -c "…"`, `sh -c '…'`, `eval "…"`) or a substitution is still a push.
    ("bash -c, double quotes", 'bash -c "git push -u origin chore/x"', {}, 0, 4, None),
    ("sh -c, single quotes", "sh -c 'git push -u origin chore/x'", {}, 0, 4, None),
    ("bash -lc", 'bash -lc "git push origin HEAD"', {}, 0, 4, None),
    ("eval", 'eval "git push"', {}, 0, 4, None),
    ("timeout wrapping bash -c", 'timeout 3000 bash -c "git push origin HEAD"', {}, 0, 4, None),
    ("sudo wrapping bash -c", "sudo bash -c 'git push'", {}, 0, 4, None),
    ("push after && inside bash -c", 'bash -c "git add -A && git push"', {}, 0, 4, None),
    ("push inside $( )", 'echo $(git push origin HEAD)', {}, 0, 4, None),
    ("push inside backticks", "echo `git push origin HEAD`", {}, 0, 4, None),
    ("push inside a subshell", "(git push origin HEAD)", {}, 0, 4, None),
    ("push in a braces group", "{ git push; }", {}, 0, 4, None),
    ("push after a newline", "echo hi\ngit push", {}, 0, 4, None),
    ("push after a semicolon without a space", "true;git push", {}, 0, 4, None),
    ("push after && without a space", "true&&git push", {}, 0, 4, None),
    ("push through env and a variable assignment", "env GIT_TRACE=1 git push", {}, 0, 4, None),
    # Quoted or escaped values of git's own options may contain spaces.
    ("-C with a double-quoted path with a space", 'git -C "/tmp/a b" push origin x', {}, 0, 4, None),
    ("-C with a single-quoted path with a space", "git -C '/tmp/a b' push origin x", {}, 0, 4, None),
    ("-C with a backslash-escaped space", "git -C /tmp/a\\ b push origin x", {}, 0, 4, None),
    ("-c with a quoted value with a space", 'git -c user.name="A B" push origin x', {}, 0, 4, None),
    ("two -C options", 'git -C /tmp/a -C "b c" push', {}, 0, 4, None),
    ("--git-dir with a separate quoted value", 'git --git-dir "/tmp/a b/.git" push', {}, 0, 4, None),
    ("--git-dir= with a quoted value", 'git --git-dir="/tmp/a b/.git" push', {}, 0, 4, None),
    ("--no-pager before push", "git --no-pager push", {}, 0, 4, None),
    ("-C quoted path inside bash -c with escaped quotes", 'bash -c "git -C \\"/tmp/a b\\" push origin x"', {}, 0, 4, None),
    ("a path that contains the word push", "git -C /tmp/push-dir push origin feat", {}, 0, 4, None),
    # The wrappers must not hide the denials and skips either.
    ("wrapped --all is denied", 'bash -c "git push --all origin"', {}, 2, 0, None),
    ("wrapped push of another branch with Kotlin is denied",
     "bash -c 'git push origin feat'", {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    ("wrapped push of another branch, double quotes and a refspec",
     'sh -c "git push origin +feat:feat"', {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    ("push of another branch inside $( ) is denied",
     "echo $(git push origin feat)", {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    ("wrapped push of another branch with only Markdown is allowed",
     "bash -c 'git push origin feat'", {"changed_file": "docs/notes.md", "foreign": "md"}, 0, 0, None),
    ("wrapped switch and push is denied",
     'sh -c "git switch feat && git push"', {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    ("wrapped dry run is skipped", 'bash -c "git push --dry-run"', {}, 0, 0, None),
    ("wrapped --delete is skipped", "bash -c 'git push origin --delete old'", {}, 0, 0, None),
    ("wrapped colon delete is skipped", 'bash -c "git push origin :old"', {}, 0, 0, None),
    ("delete refspec after a path that contains the word push", "git -C /tmp/push-dir push origin :old", {}, 0, 0, None),
    ("wrapped commit and push, a passed HEAD does not skip the checks",
     'bash -c "git commit -am x && git push"', {"upstream_at_head": True, "dirty": True, "prior_pass": True}, 0, 4, None),
    ("wrapped push on a passed HEAD is skipped", 'bash -c "git push"', {"prior_pass": True}, 0, 0, None),
    ("wrapped push with only a Markdown change", "bash -c 'git push'", {"changed_file": "docs/notes.md"}, 0, 0, None),
    # Hostile or malformed commands must neither hang the hook (the match must not backtrack exponentially) nor push.
    ("many backslashes after -C do not hang, and the pairs are a real path", "git -C " + "\\" * 3000 + " push", {}, 0, 4, None),
    ("many escaped quotes do not hang", "git " + '\\"' * 3000 + " push", {}, 0, 0, None),
    ("many escaped spaces after -C do not hang", "git -C " + "\\ " * 2000 + " push", {}, 0, 4, None),
    ("escaped spaces that swallow the word push are a path, not a push", "git -C " + "\\ " * 2000 + "push", {}, 0, 0, None),
    ("many quoted -C options then a push", "git " + '-C "a b" ' * 300 + "push", {}, 0, 4, None),
    ("an unbalanced quote does not hang", 'bash -c "git push ' + "'" * 3000, {}, 0, 4, None),
    ("a very long argument list is still a push", "git push " + "a " * 5000, {}, 0, 4, None),
    # git written as a quoted or escaped word still pushes (a backslash is the usual way to skip an alias).
    ("quoted command word, double quotes", '"git" push -u origin chore/x', {}, 0, 4, None),
    ("quoted command word, single quotes", "'git' push -u origin chore/x", {}, 0, 4, None),
    ("backslash before git", "\\git push -u origin chore/x", {}, 0, 4, None),
    ("quoted path to git", '"/usr/bin/git" push -u origin chore/x', {}, 0, 4, None),
    ("quoted command word keeps its later quoted refspec",
     '"git" push origin "feat"', {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    # What follows the closing quote of a wrapper belongs to the wrapper, not to the push inside it.
    ("a flag after the wrapper does not make the push a dry run", 'bash -c "git push" -n', {}, 0, 4, None),
    ("a flag after the wrapper does not make the push a delete", "bash -c 'git push origin feat' --delete", {}, 0, 4, None),
    ("a redirect and flag after the wrapper", 'bash -c "git push origin HEAD" > /tmp/o -n', {}, 0, 4, None),
    ("the wrapper cut keeps a deny: another branch with Kotlin",
     "bash -c 'git push origin feat' --delete", {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    ("a flag inside the wrapper still makes a dry run", 'bash -c "git push --dry-run" -x', {}, 0, 0, None),
    # Options that take a value must have one parse only: these used to backtrack exponentially (2^n at n pairs).
    ("40 -C options whose value starts with a dash, then pull", "git " + "-C -x " * 40 + "pull", {}, 0, 0, None),
    ("40 -C options whose value starts with a dash, then push", "git " + "-C -x " * 40 + "push", {}, 0, 4, None),
    ("40 -C -C pairs, then no subcommand", "git " + "-C -C " * 40 + "x", {}, 0, 0, None),
    ("40 --git-dir options with a dash value, then pull", "git " + "--git-dir -x " * 40 + "pull", {}, 0, 0, None),
    ("40 -c options with dash values, then push", "git " + "-c -x " * 40 + "push", {}, 0, 4, None),
    ("mixed options 60 times, then pull", "git " + "-C a --git-dir b -c k=v -x " * 60 + "pull", {}, 0, 0, None),
    ("-C as the last word before push takes push as its value", "git -C push", {}, 0, 0, None),
    # The hook parses every Bash command: a long run without spaces must not be rescanned from each quote (O(n^2)).
    ("100 KB of minified JSON in a heredoc, no push", "cat > /tmp/x.json <<EOF\n" + '{"a":"b","c":["d","e"]}' * 4300 + "\nEOF",
     {}, 0, 0, None),
    ("100 KB of minified JSON with one push word", "cat > /tmp/x.json <<EOF\n" + '{"a":"b","c":["d","e"]}' * 4300 + " push\nEOF",
     {}, 0, 0, None),
    ("100 KB of quotes with slashes and a push word", ('"a/' * 33000) + " push", {}, 0, 0, None),
    ("100 KB path-like run before a push", ("a/" * 50000) + GIT_WORD + " push origin x", {}, 0, 4, None),
    ("a long quoted run then a real push", "echo " + '"x"' * 30000 + " && git push", {}, 0, 4, None),
    # Pushes found by reading the command as shell words: the argument split follows quoting, wrappers and redirections.
    ("wrapper cut must not drop a refspec: a delete and a quoted branch",
     'bash -c "git push origin :old \\"feat\\""', {}, 0, 4, None),
    ("a quote that closes an earlier word is not a wrapper", '"$HOME"/bin/git push origin :old "feat"', {}, 0, 4, None),
    ("wrapped --all with a quoted remote is denied", 'bash -c "git push \\"origin\\" --all"', {}, 2, 0, None),
    ("--mirror with a quoted remote after a quoted path is denied", '"$HOME"/bin/git push "origin" --mirror', {}, 2, 0, None),
    ("quoted branch after a quoted path is judged as another branch",
     '"$HOME"/bin/git push origin "feat"', {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    ("quoted push option inside a wrapper, another branch with Kotlin",
     'bash -c "git push -o \\"ci.skip\\" origin feat"', {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    # What follows a wrapper belongs to the wrapper, wherever the push sits inside it.
    ("push after cd && inside a wrapper, flag after the wrapper", 'bash -c "cd . && git push -u origin chore/x" -n', {}, 0, 4, None),
    ("push after a semicolon inside a wrapper, flag after the wrapper", 'bash -c "git fetch; git push" -n', {}, 0, 4, None),
    ("single-quoted wrapper, push after a semicolon, --delete after", "sh -c 'true; git push origin feat' --delete", {}, 0, 4, None),
    ("a dry run inside the wrapper still counts", 'bash -c "cd . && git push --dry-run" -x', {}, 0, 0, None),
    ("nested wrappers", """bash -c 'sh -c "git push -u origin chore/x"'""", {}, 0, 4, None),
    ("eval of a quoted command with a branch switch", 'eval "git switch feat && git push"',
     {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    # The git word and the subcommand may be spelled in ways the shell turns into the same words.
    ("capital Git (the default macOS file system ignores case)", "Git push -u origin chore/x", {}, 0, 4, None),
    ("upper case GIT", "GIT push", {}, 0, 4, None),
    ("empty quotes inside the git word", 'gi""t push', {}, 0, 4, None),
    ("one letter quoted", '"g"it push', {}, 0, 4, None),
    ("empty quotes after the git word", 'git"" push', {}, 0, 4, None),
    ("escape inside the git word", "g\\it push", {}, 0, 4, None),
    ("quoted subcommand", 'git "push" origin chore/x', {}, 0, 4, None),
    ("split quotes inside the subcommand", 'git pu""sh', {}, 0, 4, None),
    # Redirections and substitutions are not arguments of the push.
    ("a redirect target that looks like a flag", "git push -u origin chore/x > -n", {}, 0, 4, None),
    ("a file descriptor redirect", "git push 2>&1", {}, 0, 4, None),
    ("a dry run with a redirect", "git push --dry-run > out.txt", {}, 0, 0, None),
    ("a flag inside a substitution after the push", "git push origin feat $(: -n)", {}, 0, 4, None),
    # Stalls: the hook parses every Bash command, so none of these may be slow (MAX_SECONDS) or quadratic.
    ("JSON of quoted git fragments in a heredoc, with a push word",
     "echo push; cat <<E\n[" + ('"git -C a",' * 20000) + "]\nE", {}, 0, 0, None),
    ("many git words each with a -C value, then a push", "git -C a " * 20000 + "git push", {}, 0, 4, None),
    ("many git words each with a --git-dir value, then a push", "git --git-dir a " * 20000 + "git push", {}, 0, 4, None),
    ("many -C options that swallow the next git word: the last -C takes push as its value", "git -C " * 20000 + "push", {}, 0, 0, None),
    ("many git words, then a commit and a push", "git -C a " * 20000 + " commit && git push", {}, 0, 4, None),
    ("many half-quoted flags, then a push", 'git -a"' * 20000 + " push", {}, 0, 4, None),
    ("many half-quoted flags in a long run, no push word", 'git -a"' * 20000, {}, 0, 0, None),
    # Quotes nested inside quotes, built the way the shell needs them. A push nested deeper than the parser reads
    # is not allowed through: the command is treated as unreadable and checked.
    ("wrappers nested 3 deep", nested("git push -u origin chore/x", 3), {}, 0, 4, None),
    ("wrappers nested 6 deep", nested("git push -u origin chore/x", 6), {}, 0, 4, None),
    ("wrappers nested 8 deep are checked, not skipped", nested("git push -u origin chore/x", 8), {}, 0, 4, None),
    ("wrappers nested 8 deep, a dry run inside is still checked (unreadable)", nested("git push --dry-run", 8), {}, 0, 4, None),
    ("wrappers nested 3 deep, a dry run inside", nested("git push --dry-run", 3), {}, 0, 0, None),
    ("wrappers nested 3 deep, another branch with Kotlin",
     nested("git push origin feat", 3), {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    ("a megabyte of arguments after a push", "git push " + "a " * 400000, {}, 0, 4, None),
    # A backslash and a newline vanish in the shell, so they join the two halves of a word (and of a flag).
    ("line continuation inside the git word", "gi\\\nt push -u origin chore/x", {}, 0, 4, None),
    ("line continuation inside the subcommand", "git pu\\\nsh -u origin chore/x", {}, 0, 4, None),
    ("an escaped backslash, then a push on the next line", "echo a\\\\\ngit push -u origin chore/x", {}, 0, 4, None),
    ("line continuation inside --all is still denied", "git push -\\\n-all origin", {}, 2, 0, None),
    ("line continuation inside double quotes", 'git "pu\\\nsh" -u origin chore/x', {}, 0, 4, None),
    ("line continuation inside a wrapper", "bash -c 'git pu\\\nsh -u origin chore/x'", {}, 0, 4, None),
    ("a backslash-newline stays literal in single quotes: not the push subcommand", "git 'pu\\\nsh' -u origin chore/x",
     {}, 0, 0, None),
    ("line continuation between the dollar and the quote", "git $\\\n'pu'sh -u origin chore/x", {}, 0, 4, None),
    ("line continuation between the dollar and an octal ANSI-C string", "git $\\\n'\\160ush' -u origin chore/x", {}, 0, 4, None),
    ("line continuation before a locale string, in a wrapper", 'bash -c "git $\\\n\\"push\\" -u origin chore/x"', {}, 0, 4, None),
    ("empty locale string inside the subcommand", 'git pu$""sh -u origin chore/x', {}, 0, 4, None),
    ("empty locale string, in a wrapper", "bash -c 'git pu$\"\"sh -u origin chore/x'", {}, 0, 4, None),
    ("zsh =git expansion", "=git push -u origin chore/x", {}, 0, 4, None),
    ("a glob in the path of git", "/usr/bin/gi[t] push -u origin chore/x", {}, 0, 4, None),
    ("a glob with a question mark in the path of git", "/usr/bin/gi? push -u origin chore/x", {}, 0, 4, None),
    ("not git: a glob that cannot match git", "/usr/bin/ls? push", {}, 0, 0, None),
    # The text inside a wrapper is read as a command, so it may split the word push the same ways.
    ("split quotes inside a single-quoted wrapper", "bash -c 'git pu\"sh\" -u origin chore/x'", {}, 0, 4, None),
    ("escape inside a double-quoted wrapper", 'bash -c "git pu\\\\sh -u origin chore/x"', {}, 0, 4, None),
    ("ANSI-C octal escape inside a wrapper", r'''bash -c "git \$'\160ush' -u origin chore/x"''', {}, 0, 4, None),
    ("split subcommand inside nested wrappers", nested("git pu\"sh\" -u origin chore/x", 3), {}, 0, 4, None),
    # A locale string or an empty substitution inside a word still spells push.
    ("locale string inside the subcommand, in a wrapper", "bash -c 'git pu$\"sh\" -u origin chore/x'", {}, 0, 4, None),
    ("locale string inside the subcommand", 'git pu$"sh" -u origin chore/x', {}, 0, 4, None),
    ("empty substitution inside the subcommand", "git pu$()sh -u origin chore/x", {}, 0, 4, None),
    ("empty backticks inside the subcommand", "git pu``sh -u origin chore/x", {}, 0, 4, None),
    ("empty substitution inside the git word", "g$()it push -u origin chore/x", {}, 0, 4, None),
    ("empty substitution inside a wrapper", "bash -c 'git pu$()sh -u origin chore/x'", {}, 0, 4, None),
    ("empty substitution that does not spell push: not a push", "echo pu$()ll && git status", {}, 0, 0, None),
    # Adjacent backticks are a closing and an opening one when a substitution is open, and nothing when none is.
    ("two backtick substitutions next to each other", "echo `echo hi``git push -u origin chore/x`", {}, 0, 4, None),
    ("two backtick substitutions, the second a switch and push", "echo `echo hi``git switch feat && git push`", {}, 2, 0, None),
    # An empty substitution inside double quotes disappears too.
    ("empty substitution inside double quotes, whole subcommand", 'git "pu$()sh" -u origin chore/x', {}, 0, 4, None),
    ("empty substitution in its own double quotes", 'git pu"$()"sh -u origin chore/x', {}, 0, 4, None),
    ("empty backticks inside double quotes", 'git "pu``sh" -u origin chore/x', {}, 0, 4, None),
    ("a substitution with only blanks inside", "git pu$( )sh -u origin chore/x", {}, 0, 4, None),
    ("a substitution with only blanks inside double quotes", 'git "pu$(  )sh" -u origin chore/x', {}, 0, 4, None),
    ("a glob that negates with a caret", "/usr/bin/gi[^x] push -u origin chore/x", {}, 0, 4, None),
    # ANSI-C ($'...') and locale ($"...") strings are words the shell decodes.
    ("ANSI-C quoted subcommand", "git $'push' -u origin chore/x", {}, 0, 4, None),
    ("locale quoted subcommand", 'git $"push" -u origin chore/x', {}, 0, 4, None),
    ("ANSI-C quoted git word", "$'git' push -u origin chore/x", {}, 0, 4, None),
    ("ANSI-C octal escapes spell the subcommand", "git $'\\160\\165\\163\\150' -u origin chore/x", {}, 0, 4, None),
    ("ANSI-C hex escapes spell the git word", "$'\\x67\\x69\\x74' push -u origin chore/x", {}, 0, 4, None),
    ("ANSI-C unicode escape in the subcommand", "git $'\\u0070ush' -u origin chore/x", {}, 0, 4, None),
    ("ANSI-C quoted branch is judged as another branch",
     "git push origin $'feat'", {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    ("ANSI-C quoted --all is denied", "git push $'--all' origin", {}, 2, 0, None),
    ("ANSI-C escaped quote does not end the string early: the switch inside stays hidden",
     "git push -u origin chore/x $'\\'; git switch feat; \\''", {}, 0, 4, None),
    ("ANSI-C string inside a wrapper", "bash -c $'git push -u origin chore/x'", {}, 0, 4, None),
    ("a dollar before a substitution is not a string", "echo $(git push -u origin chore/x)", {}, 0, 4, None),
    ("an ANSI-C string that does not spell push: not a push", "git $'status' && echo $'a\\tb'", {}, 0, 0, None),
    # git options that take a separate value must not be read as the subcommand.
    ("--attr-source with a value", "git --attr-source HEAD push -u origin chore/x", {}, 0, 4, None),
    ("--config-env with a value", "git --config-env core.x=HOME push -u origin chore/x", {}, 0, 4, None),
    ("--attr-source, another branch with Kotlin",
     "git --attr-source HEAD push origin feat", {"changed_file": "docs/notes.md", "foreign": "kt"}, 2, 0, None),
    # git accepts a unique prefix of a long option, so the denied options are denied abbreviated too.
    ("--mirr is --mirror", "git push --mirr origin", {}, 2, 0, None),
    ("--mir is --mirror", "git push --mir origin", {}, 2, 0, None),
    ("--al is --all", "git push --al origin", {}, 2, 0, None),
    ("--all inside a wrapper", "bash -c 'git push --al origin'", {}, 2, 0, None),
    ("--atomic is not --all", "git push --atomic -u origin chore/x", {}, 0, 4, None),
    # The last of a flag and its negation wins.
    ("--no-dry-run after -n pushes", "git push -n --no-dry-run -u origin chore/x", {}, 0, 4, None),
    ("--no-dry-run abbreviated after --dry-run pushes", "git push --dry-run --no-dry -u origin chore/x", {}, 0, 4, None),
    ("-n after --no-dry-run is a dry run", "git push --no-dry-run -n origin chore/x", {}, 0, 0, None),
    ("--no-delete after --delete pushes", "git push --delete --no-delete origin feat", {}, 0, 4, None),
    ("--delete after --no-delete deletes", "git push --no-delete --delete origin old", {}, 0, 0, None),
    ("--no-delete abbreviated after -d pushes", "git push -d --no-del origin feat", {}, 0, 4, None),
    # Words that only look like git or push.
    ("not a push: another word ending in git", "legit push", {}, 0, 0, None),
    ("not a push: git push-foo", "git push-foo", {}, 0, 0, None),
    ("not a push: git pushd-like word", "echo pushed", {}, 0, 0, None),
    ("not a push: git status", "git status", {}, 0, 0, None),
    ("not a push: git pull", "git pull --rebase", {}, 0, 0, None),
    ("not a push: gh pr view", "gh pr view 3", {}, 0, 0, None),
]

# The report the agent reads on a denial must say why and what to do. Each case: name, command, kwargs, want exit,
# text the report (stderr) must contain, text it must not contain (the filter drops the stub gradle's task line). A broken failure filter or an empty report would pass the cases above.
REPORTS = [
    ("coverage floor fails: the trimmed gradle report names the failing task and drops task noise", PUSH,
     {"fail": "jacocoCoverageVerification"}, 2,
     ["PUSH DENIED", "Execution failed for task ':core:domain:jacocoCoverageVerification'", "Nothing was pushed",
      "Rule violated for bundle core:domain", "expected minimum is 0.80"],
     ["> Task :"]),
    ("iOS simulator tests fail: the trimmed gradle report names the failing task", PUSH,
     {"fail": "iosSimulatorArm64Test"}, 2,
     ["PUSH DENIED", "Execution failed for task ':core:domain:iosSimulatorArm64Test'", "FooTest > bar FAILED"],
     ["> Task :"]),
    ("device tests fail: the trimmed gradle report names the failing task", PUSH,
     {"fail": "connectedAndroidDeviceTest"}, 2,
     ["PUSH DENIED", "Execution failed for task ':core:domain:connectedAndroidDeviceTest'", "FooTest > bar FAILED"],
     ["> Task :"]),
    ("--all: says to push one branch at a time", "git push --all origin", {}, 2,
     ["PUSH DENIED", "--all/--mirror", "one at a time"], []),
    ("another branch with Kotlin: names it and says to check it out",
     "git push origin feat", {"changed_file": "docs/notes.md", "foreign": "kt"}, 2,
     ["PUSH DENIED", "feat is not the checked-out branch", "git switch feat"], []),
    ("switch and push: says to run the switch first", "git switch feat && git push", {}, 2,
     ["PUSH DENIED", "switches branch and pushes", "own command"], []),
]

failed = 0
for name, command, kwargs, want_code, parts, absent in REPORTS:
    code, _, _, report = run(command, **kwargs)
    ok = code == want_code and all(part in report for part in parts) and not any(part in report for part in absent)
    failed += not ok
    print(f"{'ok  ' if ok else 'FAIL'} exit={code} report has {len(parts)} expected parts  {name}")
    if not ok:
        print("     report was:", report.strip()[:300].replace("\n", " | "))
for name, command, kwargs, want_code, want_calls, must_contain in CASES:
    code, calls, elapsed, _ = run(command, **kwargs)
    # The stubs return at once, so the hook itself must answer fast: a slow match stalls every Bash command.
    ok = code == want_code and len(calls) == want_calls and elapsed < MAX_SECONDS
    if must_contain and ok:
        ok = all(any(part in line for part in must_contain.split("|")) for line in calls)
    failed += not ok
    print(f"{'ok  ' if ok else 'FAIL'} exit={code} calls={len(calls)} (want {want_code}, {want_calls}) "
          f"{elapsed:.2f}s  {name}")
sys.exit(1 if failed else 0)
