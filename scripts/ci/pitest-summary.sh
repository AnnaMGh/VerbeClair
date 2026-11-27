#!/usr/bin/env bash
# Writes the Pitest mutation scores as a Markdown table, for the run's summary (Notion N-18). Report only: no score
# fails this script. It fails when a module has no readable report, so that a score can't go missing unnoticed.
#
# Mutation score: detected mutants out of all of them. Test strength: detected mutants out of those that a test covers.
#
# Usage: pitest-summary.sh <module-dir>...        (for example core/domain core/database)

set -euo pipefail

[ $# -gt 0 ] || {
  echo "pitest-summary: no module given" >&2
  exit 1
}

python3 -I - "$@" <<'PY'
import sys
import xml.etree.ElementTree as ET


def percent(part, whole):
    # Rounded half up. A dash when there is nothing to divide by.
    return f"{(200 * part + whole) // (2 * whole)}%" if whole else "-"


print("### Mutation testing (Pitest, report only)")
print()
print("| Module | Mutants | Detected | Survived | No coverage | Mutation score | Test strength |")
print("|---|---:|---:|---:|---:|---:|---:|")

failed = False
for module in sys.argv[1:]:
    path = f"{module}/build/reports/pitest/mutations.xml"
    try:
        mutations = ET.parse(path).getroot().findall("mutation")
    except (OSError, ET.ParseError) as error:
        print(f"| `{module}` | no report | | | | | |")
        print(f"pitest-summary: can't read {path}: {error}", file=sys.stderr)
        failed = True
        continue
    total = len(mutations)
    detected = sum(m.get("detected") == "true" for m in mutations)
    survived = sum(m.get("status") == "SURVIVED" for m in mutations)
    uncovered = sum(m.get("status") == "NO_COVERAGE" for m in mutations)
    print(
        f"| `{module}` | {total} | {detected} | {survived} | {uncovered} "
        f"| {percent(detected, total)} | {percent(detected, total - uncovered)} |"
    )

sys.exit(1 if failed else 0)
PY
