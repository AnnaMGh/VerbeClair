#!/usr/bin/env bash
# Checks the decisions of changed-code.sh. Run: bash scripts/ci/changed-code-test.sh
set -u
cd "$(dirname "$0")"
fail=0
check() { # name expected input
  actual=$(printf '%s' "$3" | ./changed-code.sh --stdin)
  if [ "$actual" = "code=$2" ]; then echo "ok   $1"; else echo "FAIL $1: expected code=$2, got $actual"; fail=1; fi
}
check "docs only"            false $'openspec/changes/x/tasks.md\nREADME.md\n.claude/settings.json'
check "kotlin"               true  $'shared/src/commonMain/Foo.kt'
check "mixed"                true  $'README.md\nshared/build.gradle.kts'
check "workflow"             true  '.github/workflows/ci.yml'
check "ci script"            true  'scripts/ci/run-device-tests.sh'
check "version catalog"      true  'gradle/libs.versions.toml'
check "nested markdown"      false 'core/designsystem/README.md'
check "empty diff"           true  ''
# without --stdin: no base, all-zero base (new branch) and an unknown ref fail safe to code=true
for base in "" "0000000000000000000000000000000000000000" "no-such-ref"; do
  actual=$(./changed-code.sh "$base")
  if [ "$actual" = "code=true" ]; then echo "ok   base '$base'"; else echo "FAIL base '$base': $actual"; fail=1; fi
done
exit $fail
