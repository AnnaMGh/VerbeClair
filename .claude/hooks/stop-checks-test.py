#!/usr/bin/env python3
"""Decisions of stop-checks.sh and ktlint-format.sh. Run: python3 .claude/hooks/stop-checks-test.py

Each case runs the hook in a throwaway git repository whose `gradlew` is a stub that logs its calls (and fails while a
`fail` file exists). It checks the exit code (2 = blocked, 0 = allowed) and how many times Gradle ran.
"""
import json
import os
import stat
import subprocess
import sys
import tempfile

HOOKS = os.path.dirname(os.path.abspath(__file__))
STOP = os.path.join(HOOKS, "stop-checks.sh")
FORMAT = os.path.join(HOOKS, "ktlint-format.sh")

GRADLEW = """#!/bin/sh
echo "$*" >> "$(dirname "$0")/calls.log"
if [ -f "$(dirname "$0")/fail" ]; then
    echo "e: A.kt:1:1 boom"
    echo "> Task :x FAILED"
    exit 1
fi
exit 0
"""


def git(root, *args):
    return subprocess.run(["git", "-C", root, "-c", "user.name=t", "-c", "user.email=t@t", *args],
                          text=True, capture_output=True, check=True).stdout.strip()


def make_repo(root):
    git(root, "init", "-q")
    with open(os.path.join(root, ".gitignore"), "w") as f:
        f.write("calls.log\nfail\n.claude/state/\n")
    with open(os.path.join(root, "README.md"), "w") as f:
        f.write("x\n")
    full = os.path.join(root, "gradlew")
    with open(full, "w") as f:
        f.write(GRADLEW)
    os.chmod(full, os.stat(full).st_mode | stat.S_IEXEC)
    git(root, "add", "-A")
    git(root, "commit", "-q", "-m", "base")
    git(root, "update-ref", "refs/remotes/origin/dev", "HEAD")


def write(root, path, body="x\n"):
    full = os.path.join(root, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as f:
        f.write(body)


def stop(root, active=False, session="s1"):
    event = json.dumps({"session_id": session, "stop_hook_active": active})
    env = {**os.environ, "CLAUDE_PROJECT_DIR": root}
    return subprocess.run(["sh", STOP], input=event, text=True, capture_output=True, env=env)


def gradle_calls(root):
    path = os.path.join(root, "calls.log")
    return len(open(path).read().splitlines()) if os.path.exists(path) else 0


def case_markdown_only(root):
    write(root, "README.md", "changed\n")
    result = stop(root)
    return result.returncode == 0 and gradle_calls(root) == 0


def case_kotlin_change_passes(root):
    write(root, "A.kt")
    result = stop(root)
    return result.returncode == 0 and gradle_calls(root) == 1


def case_kotlin_change_fails_and_blocks(root):
    write(root, "A.kt")
    write(root, "fail")
    result = stop(root)
    return result.returncode == 2 and "boom" in result.stderr and "block 1 of 3" in result.stderr


def case_fourth_stop_allowed(root):
    write(root, "A.kt")
    write(root, "fail")
    codes = [stop(root).returncode] + [stop(root, active=True).returncode for _ in range(2)]
    fourth = stop(root, active=True)
    message = json.loads(fourth.stdout)["systemMessage"] if fourth.stdout.strip() else ""
    return codes == [2, 2, 2] and fourth.returncode == 0 and "boom" in message


def case_new_turn_resets_counter(root):
    write(root, "A.kt")
    write(root, "fail")
    for _ in range(3):
        stop(root, active=True)
    # A stop that is not a continuation (stop_hook_active false) starts a new turn and its own count.
    result = stop(root, active=False)
    return result.returncode == 2 and "block 1 of 3" in result.stderr


def case_committed_failure_not_forgotten(root):
    write(root, "A.kt")
    write(root, "fail")
    git(root, "add", "-A")
    git(root, "commit", "-q", "-m", "kt")
    first = stop(root)
    second = stop(root, active=True)
    return first.returncode == 2 and second.returncode == 2 and "block 2 of 3" in second.stderr


def case_non_ascii_path(root):
    write(root, "dir é/B.kt")
    write(root, "fail")
    return stop(root).returncode == 2 and gradle_calls(root) == 1


def case_path_with_spaces(root):
    write(root, "my dir/B.kt")
    write(root, "fail")
    return stop(root).returncode == 2 and gradle_calls(root) == 1


def case_no_repeat_after_clean_commit(root):
    write(root, "A.kt")
    git(root, "add", "-A")
    git(root, "commit", "-q", "-m", "kt")
    first = stop(root)
    second = stop(root)
    return first.returncode == 0 and second.returncode == 0 and gradle_calls(root) == 1


def fmt(root, file_path):
    event = json.dumps({"tool_input": {"file_path": file_path}})
    env = {**os.environ, "CLAUDE_PROJECT_DIR": root}
    return subprocess.run(["sh", FORMAT], input=event, text=True, capture_output=True, env=env).returncode


def case_format_ignores_markdown(root):
    write(root, "README.md")
    return fmt(root, os.path.join(root, "README.md")) == 0


def case_format_without_classpath_exits_0(root):
    write(root, "A.kt")
    return fmt(root, os.path.join(root, "A.kt")) == 0


def case_format_missing_file_exits_0(root):
    return fmt(root, os.path.join(root, "Missing.kt")) == 0


def case_format_reports_unfixable_violation(root):
    # A stub `java` stands in for ktlint: it prints one violation and fails, as ktlint does for a wildcard import.
    write(root, "A.kt", "import a.*\n")
    write(root, "build/ktlint/cli-classpath.txt", "x.jar\n")
    write(root, "bin/java", "#!/bin/sh\necho 'A.kt:1:1: Wildcard import (standard:no-wildcard-imports)'\nexit 1\n")
    os.chmod(os.path.join(root, "bin/java"), 0o755)
    event = json.dumps({"tool_input": {"file_path": os.path.join(root, "A.kt")}})
    env = {**os.environ, "CLAUDE_PROJECT_DIR": root, "PATH": os.path.join(root, "bin") + os.pathsep + os.environ["PATH"]}
    result = subprocess.run(["sh", FORMAT], input=event, text=True, capture_output=True, env=env)
    return result.returncode == 2 and "Wildcard import" in result.stderr


CASES = [
    ("stop: Markdown-only change runs nothing", case_markdown_only),
    ("stop: Kotlin change, checks pass", case_kotlin_change_passes),
    ("stop: Kotlin change, checks fail, stop blocked with the failure", case_kotlin_change_fails_and_blocks),
    ("stop: the fourth stop is allowed and reports the failure", case_fourth_stop_allowed),
    ("stop: a new turn restarts the block count", case_new_turn_resets_counter),
    ("stop: a failure in committed work is not forgotten", case_committed_failure_not_forgotten),
    ("stop: Kotlin file with a non-ASCII path is checked", case_non_ascii_path),
    ("stop: Kotlin file in a directory with a space is checked", case_path_with_spaces),
    ("stop: a passed commit is not checked again", case_no_repeat_after_clean_commit),
    ("format: Markdown passes through", case_format_ignores_markdown),
    ("format: no ktlint classpath exits 0", case_format_without_classpath_exits_0),
    ("format: missing file exits 0", case_format_missing_file_exits_0),
    ("format: an unfixable violation is reported with exit 2", case_format_reports_unfixable_violation),
]

failed = 0
for name, case in CASES:
    with tempfile.TemporaryDirectory() as root:
        make_repo(root)
        ok = bool(case(root))
    failed += not ok
    print(f"{'ok  ' if ok else 'FAIL'} {name}")
sys.exit(1 if failed else 0)
