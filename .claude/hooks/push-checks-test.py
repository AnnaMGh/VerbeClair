#!/usr/bin/env python3
"""Decisions of push-checks.sh. Run: python3 .claude/hooks/push-checks-test.py

Each case feeds the hook a Bash command as the hook event JSON, in a throwaway git repository whose `gradlew` and
emulator script are stubs that log their calls, and checks the exit code (2 = denied, 0 = allowed) and which stub
commands ran.
"""
import json
import os
import stat
import subprocess
import sys
import tempfile

HOOK = os.path.join(os.path.dirname(os.path.abspath(__file__)), "push-checks.sh")

GRADLEW = """#!/bin/sh
echo "gradlew ANDROID_SERIAL=${ANDROID_SERIAL:-none} $*" >> "$(dirname "$0")/calls.log"
if [ -f "$(dirname "$0")/fail" ] && echo "$*" | grep -q "$(cat "$(dirname "$0")/fail")"; then
    echo "> Task :core:domain:$(cat "$(dirname "$0")/fail") FAILED"
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
        for _ in range(2 if twice else 1):
            try:
                code = subprocess.run(["sh", HOOK], input=event, text=True, capture_output=True, env=env,
                                      timeout=30).returncode
            except subprocess.TimeoutExpired:
                # A hook that hangs on a hostile command stalls the agent: report it as a failed case (-1).
                code = -1
        log_path = os.path.join(root, "calls.log")
        calls = open(log_path).read().splitlines() if os.path.exists(log_path) else []
        return code, calls


PUSH = "git push -u origin chore/x"
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
    # Words that only look like git or push.
    ("not a push: another word ending in git", "legit push", {}, 0, 0, None),
    ("not a push: git push-foo", "git push-foo", {}, 0, 0, None),
    ("not a push: git pushd-like word", "echo pushed", {}, 0, 0, None),
    ("not a push: git status", "git status", {}, 0, 0, None),
    ("not a push: git pull", "git pull --rebase", {}, 0, 0, None),
    ("not a push: gh pr view", "gh pr view 3", {}, 0, 0, None),
]

failed = 0
for name, command, kwargs, want_code, want_calls, must_contain in CASES:
    code, calls = run(command, **kwargs)
    ok = code == want_code and len(calls) == want_calls
    if must_contain and ok:
        ok = all(any(part in line for part in must_contain.split("|")) for line in calls)
    failed += not ok
    print(f"{'ok  ' if ok else 'FAIL'} exit={code} calls={len(calls)} (want {want_code}, {want_calls})  {name}")
sys.exit(1 if failed else 0)
