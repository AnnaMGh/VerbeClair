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
echo "Focused window before the tests:"
show_focus

status=0
./gradlew connectedAndroidDeviceTest -Dorg.gradle.jvmargs=-Xmx1536M --stacktrace || status=$?

echo "Focused window after the tests:"
show_focus

./gradlew --stop || true
adb emu kill || true
for _ in $(seq 1 15); do
  pgrep -f qemu-system >/dev/null || break
  sleep 2
done
pkill -9 -f qemu-system || true

exit "$status"
