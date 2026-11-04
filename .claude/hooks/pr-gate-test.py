#!/usr/bin/env python3
"""Decisions of pr-gate.sh. Run: python3 .claude/hooks/pr-gate-test.py

Each case feeds the hook a Bash command as the hook event JSON, in a throwaway git repository with no
/finalize marker, and checks the exit code: 2 = denied, 0 = allowed.
"""
import json
import os
import subprocess
import sys
import tempfile

HOOK = os.path.join(os.path.dirname(os.path.abspath(__file__)), "pr-gate.sh")

CASES = [
    ("single line, base dev", "gh pr create --base dev --title t", 2),
    ("no --base", "gh pr create --title t", 2),
    ("--base=dev", "gh pr create --base=dev --title t", 2),
    ("&& chain", "git push && gh pr create --base dev", 2),
    ("newline-separated", "git push -u origin x\ngh pr create --base dev --title t", 2),
    ("env assignment and -R", "FOO=1 gh -R a/b pr create --base dev", 2),
    ("heredoc with an apostrophe, base dev",
     "gh pr create --base dev --body-file - <<'EOF'\nIt's done\nEOF", 2),
    ("heredoc with an apostrophe, no base",
     "gh pr create --title t --body-file - <<'EOF'\nIt's done\nEOF", 2),
    ("heredoc with an apostrophe, base main",
     "gh pr create --base main --body-file - <<'EOF'\nIt's done\nEOF", 0),
    ("base main", "gh pr create --base main --title t", 0),
    ("gh pr view", "echo hi\ngh pr view 3", 0),
    ("unrelated multi-line", "git status\ngit log --oneline", 0),
    ("quoted mention", 'git commit -m "docs: mention gh pr create --base dev in text"', 0),
]


def run(command: str) -> int:
    with tempfile.TemporaryDirectory() as root:
        subprocess.run(["git", "init", "-q", root], check=True)
        subprocess.run(["git", "-C", root, "-c", "user.name=t", "-c", "user.email=t@t",
                        "commit", "-q", "--allow-empty", "-m", "x"], check=True)
        event = json.dumps({"tool_input": {"command": command}})
        result = subprocess.run(["sh", HOOK], input=event, text=True, capture_output=True,
                                env={**os.environ, "CLAUDE_PROJECT_DIR": root})
        return result.returncode


failed = 0
for name, command, want in CASES:
    got = run(command)
    ok = got == want
    failed += not ok
    print(f"{'ok  ' if ok else 'FAIL'} exit={got} (want {want})  {name}")
sys.exit(1 if failed else 0)
