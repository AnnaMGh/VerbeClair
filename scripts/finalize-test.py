#!/usr/bin/env python3
"""Decisions of scripts/finalize.sh. Run: python3 scripts/finalize-test.py

Each case runs the script in a throwaway git repository whose `gradlew` and emulator script are stubs that log their
calls. A `fail-task` file makes the stub Gradle fail for the task it names, and a `device-fail` file makes the device
tests fail with a report naming the class inside it (once, when a `once` file exists). It checks the exit code, the
steps that ran and whether .claude/state/finalize-pass was written.
"""
import os
import shutil
import stat
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
SCRIPT = os.path.join(HERE, "finalize.sh")
FLAKY = "com.vamvisions.verbeclair.di.AppKoinGraphTest"

GRADLEW = """#!/bin/sh
d=$(dirname "$0")
echo "${ANDROID_SERIAL:-none}: $*" >> "$d/calls.log"
case "$*" in
*connectedAndroidDeviceTest*)
    if [ -f "$d/device-fail" ]; then
        mkdir -p "$d/shared/build/outputs/androidTest-results/connected"
        printf '<testsuite><testcase name="t" classname="%s"><failure>boom</failure></testcase></testsuite>' \\
            "$(cat "$d/device-fail")" >"$d/shared/build/outputs/androidTest-results/connected/r.xml"
        [ -f "$d/once" ] && rm -f "$d/device-fail" "$d/once"
        echo "> There were failing tests"
        exit 1
    fi
    rm -rf "$d/shared"
    ;;
esac
if [ -f "$d/fail-task" ] && echo "$*" | grep -q "$(cat "$d/fail-task")"; then
    echo "> Task :x FAILED"
    echo "e: A.kt:1:1 boom"
    exit 1
fi
exit 0
"""

EMULATOR = """#!/bin/sh
echo "emulator $*" >> "$(dirname "$0")/../../calls.log"
[ -f "$(dirname "$0")/../../emulator-fail" ] && { echo "no boot"; exit 1; }
exit 0
"""


def git(root, *args):
    return subprocess.run(["git", "-C", root, "-c", "user.name=t", "-c", "user.email=t@t", *args],
                          text=True, capture_output=True, check=True).stdout.strip()


def make_exec(path, body):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(body)
    os.chmod(path, os.stat(path).st_mode | stat.S_IEXEC)


def make_repo(root):
    git(root, "init", "-q")
    with open(os.path.join(root, ".gitignore"), "w") as f:
        f.write("calls.log\nfail-task\ndevice-fail\nonce\nemulator-fail\nshared/\n.claude/state/\n")
    make_exec(os.path.join(root, "gradlew"), GRADLEW)
    make_exec(os.path.join(root, "scripts", "emulator", "verbeclair-emulator.sh"), EMULATOR)
    shutil.copy(SCRIPT, os.path.join(root, "scripts", "finalize.sh"))
    git(root, "add", "-A")
    git(root, "commit", "-q", "-m", "base")


def write(root, name, body):
    with open(os.path.join(root, name), "w") as f:
        f.write(body)


def run(root):
    env = {k: v for k, v in os.environ.items() if k != "ANDROID_SERIAL"}
    return subprocess.run(["sh", os.path.join(root, "scripts", "finalize.sh")], text=True, capture_output=True,
                          env=env, cwd=root)


def calls(root):
    path = os.path.join(root, "calls.log")
    return open(path).read().splitlines() if os.path.exists(path) else []


def marker(root):
    path = os.path.join(root, ".claude", "state", "finalize-pass")
    return open(path).read().strip() if os.path.exists(path) else None


def case_all_pass_writes_the_marker(root):
    result = run(root)
    log = calls(root)
    return (result.returncode == 0 and marker(root) == git(root, "rev-parse", "HEAD")
            and "READY_TO_PUSH" in result.stdout and len(log) == 5
            and any(line.startswith("emulator-5574: ") and "connectedAndroidDeviceTest" in line for line in log)
            and not any(line.startswith("emulator-5574: ") and "connected" not in line for line in log))


def case_dirty_tree_runs_nothing(root):
    write(root, "A.kt", "x\n")
    result = run(root)
    return result.returncode == 1 and not calls(root) and marker(root) is None and "step 0" in result.stderr


def case_first_failure_stops_the_run(root):
    write(root, "fail-task", "build")
    result = run(root)
    return (result.returncode == 1 and "step 2" in result.stderr and "boom" in result.stderr
            and len(calls(root)) == 2 and marker(root) is None)


def case_failure_leaves_an_old_marker_alone(root):
    os.makedirs(os.path.join(root, ".claude", "state"))
    write(root, os.path.join(".claude", "state", "finalize-pass"), "oldsha\n")
    write(root, "fail-task", "jacoco")
    result = run(root)
    return result.returncode == 1 and "step 3" in result.stderr and marker(root) == "oldsha"


def case_emulator_failure_blocks(root):
    write(root, "emulator-fail", "")
    result = run(root)
    return result.returncode == 1 and "step 4" in result.stderr and marker(root) is None


def case_flaky_test_is_rerun_once(root):
    write(root, "device-fail", FLAKY)
    write(root, "once", "")
    result = run(root)
    device_runs = [line for line in calls(root) if "connectedAndroidDeviceTest" in line]
    return result.returncode == 0 and len(device_runs) == 2 and marker(root) == git(root, "rev-parse", "HEAD")


def case_flaky_test_that_keeps_failing_blocks(root):
    write(root, "device-fail", FLAKY)
    result = run(root)
    device_runs = [line for line in calls(root) if "connectedAndroidDeviceTest" in line]
    return result.returncode == 1 and len(device_runs) == 2 and "rerun" in result.stderr and marker(root) is None


def case_another_failing_test_is_not_rerun(root):
    write(root, "device-fail", "com.vamvisions.verbeclair.SomeOtherTest")
    result = run(root)
    device_runs = [line for line in calls(root) if "connectedAndroidDeviceTest" in line]
    return result.returncode == 1 and len(device_runs) == 1 and marker(root) is None


CASES = [
    ("all steps pass: the marker holds HEAD", case_all_pass_writes_the_marker),
    ("dirty tree: nothing runs, no marker", case_dirty_tree_runs_nothing),
    ("first failure stops the run", case_first_failure_stops_the_run),
    ("a failure leaves an older marker as it was", case_failure_leaves_an_old_marker_alone),
    ("emulator does not start: blocked", case_emulator_failure_blocks),
    ("AppKoinGraphTest alone failing is rerun once and can pass", case_flaky_test_is_rerun_once),
    ("AppKoinGraphTest failing twice blocks", case_flaky_test_that_keeps_failing_blocks),
    ("any other failing test is not rerun", case_another_failing_test_is_not_rerun),
]


def main():
    failed = 0
    for name, case in CASES:
        with tempfile.TemporaryDirectory() as root:
            root = os.path.realpath(root)
            make_repo(root)
            ok = case(root)
        print(("ok   " if ok else "FAIL ") + name)
        failed += 0 if ok else 1
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
