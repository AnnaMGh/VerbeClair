#!/bin/sh
# Developer gate before a pull request to `dev`: runs the mechanical /finalize steps and, only when every one of them
# exits 0, records the commit they ran on in .claude/state/finalize-pass (gitignored), which .claude/hooks/pr-gate.sh
# checks. Run it yourself from a terminal, or from Claude Code with `! scripts/finalize.sh`.
#
# The pass is decided by exit codes alone, so no agent has to judge it. The one judgement step, the /verify review of
# the diff, is not part of this script: the /finalize skill runs it afterwards and removes the marker when it finds a
# blocking problem.
#
# Steps, cheapest first, stopping at the first failure (the marker is then left as it was):
#   0 clean working tree        3 coverage floor (jacocoCoverageVerification)
#   1 detekt, ktlint, Konsist   4 VerbeClair phone emulator (emulator-5574)
#   2 ./gradlew build           5 Android device tests on that emulator
#
# Step 5 is rerun once when its only failure is AppKoinGraphTest, which is known to be flaky.
# The Xcode that iOS tasks need is chosen by DEVELOPER_DIR, set per command and never through xcode-select.
set -u

root=$(git -C "$(dirname "$0")" rev-parse --show-toplevel 2>/dev/null) || {
    echo "BLOCKED at step 0: not inside a git repository" >&2
    exit 1
}
cd "$root" || exit 1

marker="$root/.claude/state/finalize-pass"
serial=emulator-5574
flaky_class=com.vamvisions.verbeclair.di.AppKoinGraphTest

if [ -z "${DEVELOPER_DIR:-}" ] && [ -d /Applications/Xcode.app/Contents/Developer ]; then
    DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer
    export DEVELOPER_DIR
fi

blocked() { # $1 = step, $2 = reason, $3 = optional output
    echo "BLOCKED at step $1: $2" >&2
    if [ -n "${3:-}" ]; then
        printf '%s\n' "$3" | sed "s#file://$root/##; s#$root/##" |
            grep -E '^e: |FAILED|What went wrong|Execution failed|violation|\.kt:[0-9]+|^> |coverage|Rule violated' |
            grep -v -e '^> Task ' -e '^> Run with' -e '^> Get more help' -e '^> Try' | awk '!seen[$0]++' | head -40 >&2
    fi
    exit 1
}

# Test classes with a <failure> or <error> in the instrumented test reports, one per line.
failing_device_classes() {
    find "$root" -path '*/build/outputs/androidTest-results/*' -name '*.xml' -not -path '*/.git/*' 2>/dev/null |
        python3 -c '
import sys
import xml.etree.ElementTree as ET
classes = set()
for path in sys.stdin.read().split("\n"):
    if not path:
        continue
    try:
        root = ET.parse(path).getroot()
    except (ET.ParseError, OSError):
        classes.add("?")
        continue
    for case in root.iter("testcase"):
        if case.find("failure") is not None or case.find("error") is not None:
            classes.add(case.get("classname", "?"))
print("\n".join(sorted(classes)))
'
}

if [ -n "$(git status --porcelain 2>/dev/null)" ]; then
    blocked 0 "the working tree is not clean; commit first, the pass is recorded for HEAD"
fi
head=$(git rev-parse HEAD) || blocked 0 "no commit to record"
short=$(git rev-parse --short HEAD)

output=$(./gradlew --console=plain --continue detekt ktlintCheck :tooling:konsist:test 2>&1) ||
    blocked 1 "detekt, ktlint or the Konsist tests failed" "$output"
echo "ok   1 detekt, ktlint, Konsist"

output=$(./gradlew --console=plain build 2>&1) || blocked 2 "./gradlew build failed" "$output"
echo "ok   2 build"

output=$(./gradlew --console=plain jacocoCoverageVerification 2>&1) ||
    blocked 3 "a module is below its coverage floor" "$output"
echo "ok   3 coverage floor"

output=$(scripts/emulator/verbeclair-emulator.sh start phone 2>&1) ||
    blocked 4 "the VerbeClair emulator ($serial) did not start" "$output"
echo "ok   4 emulator $serial"

# ANDROID_SERIAL keeps Gradle off every other device, including other projects' emulators.
output=$(ANDROID_SERIAL=$serial ./gradlew --console=plain connectedAndroidDeviceTest 2>&1)
if [ $? -ne 0 ]; then
    failing=$(failing_device_classes)
    if [ "$failing" = "$flaky_class" ]; then
        echo "note $flaky_class is the only failure and is known to be flaky; rerunning once"
        output=$(ANDROID_SERIAL=$serial ./gradlew --console=plain connectedAndroidDeviceTest 2>&1) ||
            blocked 5 "the Android device tests still fail after the rerun" "$output"
    else
        blocked 5 "the Android device tests failed" "$output"
    fi
fi
echo "ok   5 device tests"

# The steps ran on the working tree, so the pass only covers HEAD if neither moved nor changed while they ran.
[ "$(git rev-parse HEAD)" = "$head" ] || blocked 5 "HEAD moved while the checks ran; run it again"
[ -z "$(git status --porcelain 2>/dev/null)" ] || blocked 5 "the working tree changed while the checks ran; run it again"

mkdir -p "$(dirname "$marker")" && printf '%s\n' "$head" >"$marker" || blocked 5 "could not write $marker"
echo "READY_TO_PUSH $short (steps 0-5; the /verify review is still to run)"
