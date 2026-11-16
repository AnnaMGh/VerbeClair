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


def run(command, changed_file="shared/src/commonMain/kotlin/A.kt", fail=None, twice=False, prior_pass=False):
    with tempfile.TemporaryDirectory() as root:
        make_repo(root, changed_file)
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
            code = subprocess.run(["sh", HOOK], input=event, text=True, capture_output=True, env=env).returncode
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
    ("same HEAD already passed", PUSH, {"prior_pass": True}, 0, 0, None),
    ("second push of the same HEAD runs the checks once", PUSH, {"twice": True}, 0, 4, None),
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
