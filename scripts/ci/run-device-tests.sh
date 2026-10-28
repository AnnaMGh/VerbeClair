#!/usr/bin/env bash
# Runs the Android device tests inside android-emulator-runner, then shuts the emulator down itself.
# On the CI runner the emulator process did not exit after the action's own `adb emu kill`, so the step stayed
# open until the job timeout although every test had passed.
set -u

# Back is sent through Android's input system, which drops keys unless the window under test has focus. On the
# slow CI emulator the launcher stops responding and the system puts an "Application Not Responding" dialog in front,
# which takes the focus. Hide those dialogs, and log the focused window so a failure shows what held the focus.
show_focus() {
  adb shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp' || true
}
adb shell settings put global hide_error_dialogs 1 || true
# The system reports boot_completed while it still shows the FallbackHome placeholder. Wait for the real launcher, so
# the first tests do not run against a system that is still settling.
launcher_ready=false
for _ in $(seq 1 60); do
  if adb shell dumpsys window | grep -q 'mCurrentFocus=.*Launcher'; then
    launcher_ready=true
    break
  fi
  sleep 2
done
$launcher_ready || echo "Warning: the launcher did not take focus within 120 s; running the tests anyway."
echo "Focused window before the tests:"
show_focus

# Keep the system log for the whole run: the buffer on the device is too small to still hold the start of a failure
# when the run ends. The workflow uploads it with the test reports when the job fails.
mkdir -p build/ci-logs
adb logcat -v threadtime -b main,system,crash > build/ci-logs/logcat.txt 2>&1 &
logcat_pid=$!

status=0
./gradlew connectedAndroidDeviceTest -Dorg.gradle.jvmargs=-Xmx1536M --continue --stacktrace || status=$?

echo "Focused window after the tests:"
show_focus
kill "$logcat_pid" 2>/dev/null || true

./gradlew --stop || true
adb emu kill || true
for _ in $(seq 1 15); do
  pgrep -f qemu-system >/dev/null || break
  sleep 2
done
pkill -9 -f qemu-system || true

exit "$status"
