#!/usr/bin/env bash
# Runs the Android device tests inside android-emulator-runner, then shuts the emulator down itself.
# On the CI runner the emulator process did not exit after the action's own `adb emu kill`, so the step stayed
# open until the job timeout although every test had passed.
set -u

# Back is sent through Android's input system, which drops keys unless an awake, unlocked window has focus.
# Wake and unlock the screen, and log the state so a failure shows whether that was the cause.
show_focus() {
  adb shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp|isKeyguardShowing|mDreamingLockscreen' || true
  adb shell dumpsys power | grep -E 'mWakefulness=' || true
}
echo "Screen state before waking:"
show_focus
adb shell input keyevent KEYCODE_WAKEUP || true
adb shell wm dismiss-keyguard || true
adb shell svc power stayon true || true
echo "Screen state after waking:"
show_focus

status=0
./gradlew connectedAndroidDeviceTest -Dorg.gradle.jvmargs=-Xmx1536M --stacktrace || status=$?

echo "Screen state after the tests:"
show_focus

./gradlew --stop || true
adb emu kill || true
for _ in $(seq 1 15); do
  pgrep -f qemu-system >/dev/null || break
  sleep 2
done
pkill -9 -f qemu-system || true

exit "$status"
