#!/usr/bin/env bash
# Runs the Android device tests inside android-emulator-runner, then shuts the emulator down itself.
# On the CI runner the emulator process did not exit after the action's own `adb emu kill`, so the step stayed
# open until the job timeout although every test had passed.
set -u

status=0
./gradlew connectedAndroidDeviceTest -Dorg.gradle.jvmargs=-Xmx1536M --stacktrace || status=$?

./gradlew --stop || true
adb emu kill || true
for _ in $(seq 1 15); do
  pgrep -f qemu-system >/dev/null || break
  sleep 2
done
pkill -9 -f qemu-system || true

exit "$status"
