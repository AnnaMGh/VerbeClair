#!/bin/sh
# Start, stop or inspect the VerbeClair-only emulators.
#
# Usage: scripts/emulator/verbeclair-emulator.sh start|stop|status [phone|tablet]   (default: phone)
#
# The AVDs are named VerbeClair_* so other projects never pick them up, and each has a fixed port so its
# adb serial is known in advance (ANDROID_SERIAL=emulator-<port>) and stays clear of the default 5554.
# The emulator options and the animation settings match .github/workflows/ci.yml; only the CPU
# architecture differs (arm64 here, x86_64 on the CI runner).
set -eu

sdk=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}
emulator="$sdk/emulator/emulator"
adb="$sdk/platform-tools/adb"

action=${1:-}
target=${2:-phone}

case "$target" in
    phone) avd=VerbeClair_pixel_6_API_34 port=5574 ;;
    tablet) avd=VerbeClair_pixel_tablet_API_34 port=5576 ;;
    *) echo "unknown target '$target' (use phone or tablet)" >&2; exit 2 ;;
esac
serial="emulator-$port"

online() { "$adb" devices | grep -q "^$serial[[:space:]]*device"; }

case "$action" in
    start)
        if online; then
            echo "$serial already running ($avd)"
            exit 0
        fi
        "$emulator" -avd "$avd" -port "$port" -no-snapshot-save -no-window -gpu swiftshader_indirect \
            -noaudio -no-boot-anim -camera-back none >"${TMPDIR:-/tmp}/$avd.log" 2>&1 &
        echo "starting $avd on $serial ..."
        "$adb" -s "$serial" wait-for-device
        until [ "$("$adb" -s "$serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]; do
            sleep 2
        done
        for scale in window_animation_scale transition_animation_scale animator_duration_scale; do
            "$adb" -s "$serial" shell settings put global "$scale" 0
        done
        echo "ready: ANDROID_SERIAL=$serial"
        ;;
    stop)
        if online; then
            "$adb" -s "$serial" emu kill >/dev/null
            echo "stopped $serial"
        else
            echo "$serial is not running"
        fi
        ;;
    status)
        if online; then
            echo "$serial running ($avd)"
        else
            echo "$serial not running ($avd)"
        fi
        ;;
    *)
        echo "usage: $0 start|stop|status [phone|tablet]" >&2
        exit 2
        ;;
esac
