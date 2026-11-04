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
    ("heredoc with an apostrophe, quoted base",
     "gh pr create --base 'dev' --body-file - <<'EOF'\nIt's done\nEOF", 2),
    ("heredoc with an apostrophe, -B inside the title",
     "gh pr create --title Fix-Build --base dev --body-file - <<'EOF'\nit's done\nEOF", 2),
    ("heredoc with an apostrophe, -B flag", "gh pr create -B dev --body-file - <<'EOF'\nit's done\nEOF", 2),
    ("line continuation, base dev", "gh pr create \\\n  --base dev \\\n  --title t", 2),
    ("line continuation, base main", "gh pr create \\\n  --base main \\\n  --title t", 0),
    ("brace group", "{ gh pr create --base dev; }", 2),
    ("command prefix", "command gh pr create --base dev", 2),
    ("absolute path", "/opt/homebrew/bin/gh pr create --base dev", 2),
    ("bash -c", "bash -c 'gh pr create --base dev'", 2),
    ("bash -c, base main", "bash -c 'gh pr create --base main'", 0),
    ("comment line before the command", "# open the PR\ngh pr create --base dev --title t", 2),
    ("trailing comment before the command", "git push # push first\ngh pr create --base dev --title t", 2),
    ("timeout wrapper", "timeout 120 gh pr create --base dev --title t", 2),
    ("bash -lc", 'bash -lc "gh pr create --base dev"', 2),
    ("body in $(cat <<EOF) with an apostrophe",
     "gh pr create --base dev --body \"$(cat <<'EOF'\nIt's done\nEOF\n)\"", 2),
    ("pr new alias, base dev", "gh pr new --base dev", 2),
    ("pr new alias, no base", "gh pr new", 2),
    # Known false positives of a plain text match: a mention of the command is read as the command (no --base = deny).
    ("commit message that mentions the gate",
     "git commit -F - <<'EOF'\nDon't let gh pr create through\nEOF", 2),
    ("heredoc body that mentions another base",
     "gh pr create --base main --body-file - <<'EOF'\nuse --base dev later, it's fine\nEOF", 2),
    ("--base dev, then a body that says --base main",
     "gh pr create --base dev --body-file - <<'EOF'\nnot --base main\nEOF", 2),
    # No help exemption: a -h / --help anywhere in the text (title, body, chained command) must not open the gate.
    ("--help", "gh pr create --help", 2),
    ("-h in the body", 'gh pr create --base dev --title t --body "run df -h first"', 2),
    ("--help in the title", 'gh pr create --base dev --title "add --help flag"', 2),
    ("base from a variable", 'B=dev; gh pr create -B "$B" --title t', 2),
    ("base from a variable, --base", 'gh pr create --base "$BASE"', 2),
    ("base origin/dev", "gh pr create --base origin/dev", 2),
    ("base from a substitution", "gh pr create --base $(git config x.base)", 2),
    ("unrelated -B in a chained command, no --base", "git checkout -B feat && gh pr create --title t", 2),
    ("grep -B in the body, no --base", 'gh pr create --title t --body "see grep -B 3 output"', 2),
    ("-h in a chained command", "gh pr create --base dev && ls -h", 2),
    ("if/then", "if git push -u origin x; then gh pr create --base dev --title t; fi", 2),
    ("if/then, no base", "git push && if true; then gh pr create --title t; fi", 2),
    ("for/do", "for i in 1; do gh pr create --base dev; done", 2),
    ("backticks", "url=`gh pr create --base dev`", 2),
    ("sudo", "sudo gh pr create --base dev", 2),
    ("timeout with a signal", "timeout -s KILL 60 gh pr create --base dev", 2),
    ("custom heredoc delimiter, timeout, apostrophe",
     "timeout 60 gh pr create --base dev --body-file - <<'PR-BODY'\nit's done\nPR-BODY", 2),
    ("custom heredoc delimiter, command, VAR=",
     "cd /x && GH_REPO=a/b command gh pr create --base dev --body-file - <<'PR-BODY'\nit's done\nPR-BODY", 2),
    ("custom heredoc delimiter, mention only",
     "git commit -F - <<'MSG-END'\nDon't let gh pr create through\nMSG-END", 2),
    ("if/then, base main", "if true; then gh pr create --base main --title t; fi", 0),
    ("base main", "gh pr create --base main --title t", 0),
    ("gh pr view", "echo hi\ngh pr view 3", 0),
    ("unrelated multi-line", "git status\ngit log --oneline", 0),
    ("quoted mention", 'git commit -m "docs: mention gh pr create --base dev in text"', 2),
]


def run(command: str, marker=None) -> int:
    """marker: None = no marker file; "head" / "old" = the current / an older commit; a trailing "+nl" adds a newline."""
    with tempfile.TemporaryDirectory() as root:
        git = ["git", "-C", root, "-c", "user.name=t", "-c", "user.email=t@t"]
        subprocess.run(["git", "init", "-q", root], check=True)
        subprocess.run(git + ["commit", "-q", "--allow-empty", "-m", "first"], check=True)
        old = subprocess.run(git + ["rev-parse", "HEAD"], text=True, capture_output=True, check=True).stdout.strip()
        subprocess.run(git + ["commit", "-q", "--allow-empty", "-m", "second"], check=True)
        head = subprocess.run(git + ["rev-parse", "HEAD"], text=True, capture_output=True, check=True).stdout.strip()
        if marker is not None:
            state = os.path.join(root, ".prism", "verify", "state")
            os.makedirs(state)
            sha = old if marker.startswith("old") else head
            with open(os.path.join(state, "finalize-pass"), "w") as f:
                f.write(sha + ("\n" if marker.endswith("+nl") else ""))
        event = json.dumps({"tool_input": {"command": command}})
        result = subprocess.run(["sh", HOOK], input=event, text=True, capture_output=True,
                                env={**os.environ, "CLAUDE_PROJECT_DIR": root})
        return result.returncode


# The contract itself: a PR to dev is allowed only when the marker holds the current HEAD.
MARKER_CASES = [
    ("marker == HEAD", "gh pr create --base dev --title t", "head", 0),
    ("marker == HEAD with a trailing newline", "gh pr create --base dev --title t", "head+nl", 0),
    ("stale marker (older commit)", "gh pr create --base dev --title t", "old", 2),
    ("stale marker with a newline", "gh pr create --base dev --title t", "old+nl", 2),
    ("marker == HEAD, missing --base", "gh pr create --title t", "head", 2),
    ("marker == HEAD, wrapped command", "timeout 60 gh pr create --base dev", "head", 0),
]

failed = 0
for name, command, marker, want in MARKER_CASES:
    got = run(command, marker)
    ok = got == want
    failed += not ok
    print(f"{'ok  ' if ok else 'FAIL'} exit={got} (want {want})  {name}")
for name, command, want in CASES:
    got = run(command)
    ok = got == want
    failed += not ok
    print(f"{'ok  ' if ok else 'FAIL'} exit={got} (want {want})  {name}")
sys.exit(1 if failed else 0)
