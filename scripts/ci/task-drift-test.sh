#!/usr/bin/env bash
# Checks the decisions of task-drift.sh on made-up dry-run outputs, without Gradle.
# Run: bash scripts/ci/task-drift-test.sh
set -u
cd "$(dirname "$0")" || exit 1
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
fail=0

dry() { for task in "$@"; do printf '%s SKIPPED\n' "$task"; done; }

# Checks that the list has to run.
covered=(
  :androidApp:lintDebug :androidApp:lintAnalyzeDebug :androidApp:testDebugUnitTest :shared:testAndroidHostTest
  :core:domain:jvmTest :tooling:konsist:test :shared:detekt :androidApp:detektDebug :shared:ktlintCheck :ktlintCheck
)
# Tasks of ./gradlew build that the list doesn't have to run: exclusions and helper tasks.
not_needed=(
  :androidApp:test :androidApp:lint :androidApp:detektMain :androidApp:detektRelease :androidApp:testReleaseUnitTest
  :androidApp:lintVitalRelease :core:domain:allTests :shared:testAndroid :core:domain:iosSimulatorArm64Test
  :shared:convertXmlValueResourcesForAppleTest :shared:copyNonXmlValueResourcesForNativeTest
  :shared:generateResourceAccessorsForIosArm64Test :shared:prepareComposeResourcesTaskForIosTest
  :shared:compileTestKotlinIosArm64 :shared:linkDebugTestIosSimulatorArm64 :shared:ktlintToolingClasspath
  :tooling:konsist:testClasses :shared:checkKotlinGradlePluginConfigurationErrors :androidApp:assembleDebug
)
device=:shared:compileAndroidDeviceTest

printf '# a comment\n\n:androidApp:lintDebug\n  :androidApp:assembleDebug \ncompileAndroidDeviceTest\n' > "$tmp/names"
{ echo 'Warning: SDK processing.'; dry "${covered[@]}" "${not_needed[@]}"; } > "$tmp/build"
dry "${covered[@]}" "$device" > "$tmp/list"

# name, expected exit status, text the output has to contain, build dry run, list dry run, task list
check() {
  local out status
  out=$(./task-drift.sh --compare "$4" "$5" "$6" 2>&1)
  status=$?
  if [ "$status" -eq "$2" ] && printf '%s' "$out" | grep -qF -- "$3"; then
    echo "ok   $1"
  else
    echo "FAIL $1: expected exit $2 and '$3', got exit $status: $out"
    fail=1
  fi
}

check "full list passes" 0 "ok, ${#covered[@]} check tasks" "$tmp/build" "$tmp/list" "$tmp/names"

for removed in "${covered[@]}"; do
  dry "${covered[@]}" "$device" | grep -vxF "$removed SKIPPED" > "$tmp/less"
  check "without $removed" 1 "  $removed" "$tmp/build" "$tmp/less" "$tmp/names"
done

# New checks that nobody listed: a new module, a new target, a new lint-like task.
for added in :feature:new:jvmTest :feature:new:testAndroidHostTest :feature:new:desktopTest :feature:new:test \
  :feature:new:detektMain :feature:new:ktlintCheck :feature:new:lintDebug; do
  { cat "$tmp/build"; dry "$added"; } > "$tmp/more"
  check "new $added" 1 "  $added" "$tmp/more" "$tmp/list" "$tmp/names"
done

dry "${covered[@]}" > "$tmp/no-device"
check "no instrumented test compilation" 1 compileAndroidDeviceTest "$tmp/build" "$tmp/no-device" "$tmp/names"
printf '%s\n:androidApp:assembleDebug\n' "$device" > "$tmp/qualified"
check "instrumented test compilation of one module only" 1 compileAndroidDeviceTest \
  "$tmp/build" "$tmp/list" "$tmp/qualified"

grep -v assembleDebug "$tmp/names" > "$tmp/no-assemble"
check "no debug build" 1 ":androidApp:assembleDebug (required" "$tmp/build" "$tmp/list" "$tmp/no-assemble"

echo 'FAILURE: Build failed' > "$tmp/broken"
check "empty build dry run" 1 "no tasks in the dry run of ./gradlew build" "$tmp/broken" "$tmp/list" "$tmp/names"
check "empty list dry run" 1 "no tasks in the dry run of the task list" "$tmp/build" "$tmp/broken" "$tmp/names"
dry :androidApp:assembleDebug > "$tmp/no-checks"
check "build without checks" 1 "found no check tasks" "$tmp/no-checks" "$tmp/list" "$tmp/names"

# --print is what the CI job runs: no comments, blank lines or surrounding spaces
printed=$(./task-drift.sh --print "$tmp/names")
if [ "$printed" = $':androidApp:lintDebug\n:androidApp:assembleDebug\ncompileAndroidDeviceTest' ]; then
  echo "ok   print"
else
  echo "FAIL print: $printed"
  fail=1
fi
printf '# only a comment\n' > "$tmp/empty-list"
for list in "$tmp/empty-list" "$tmp/no-such-file"; do
  if ./task-drift.sh --print "$list" >/dev/null 2>&1; then
    echo "FAIL print of ${list##*/} passes"
    fail=1
  else
    echo "ok   print of ${list##*/} fails"
  fi
done

exit $fail
