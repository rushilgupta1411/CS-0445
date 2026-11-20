#!/usr/bin/env python3
"""
Local test harness for Assignment 5 (Meowdoku).

It compiles your Meowdoku.java, runs `java Meowdoku <puzzle-file>` on every public
puzzle in ./puzzles, and checks your output with the same validator the
autograder uses.

Usage
  python3 run_tests.py                    # compile ./Meowdoku.java, run all public puzzles
  python3 run_tests.py --src path/to/src  # the folder that contains Meowdoku.java
  python3 run_tests.py --one puzzles/pub_a3_n5.txt   # run one puzzle, show your output
  python3 run_tests.py --timeout 5        # per-puzzle time limit in seconds (default 10)

Requires Python 3.8+ and a JDK (javac and java on your PATH).
"""
import argparse
import json
import os
import shutil
import subprocess
import sys
import tempfile
from itertools import combinations


# ---------------------------------------------------------------------------
# Validator (identical logic to the autograder's)
# ---------------------------------------------------------------------------
def read_puzzle(path):
    tokens = open(path).read().split()
    n = int(tokens[0])
    return n, tokens[1:1 + n]


def check_output(puzzle_path, output, expect_solution):
    """Returns (ok, reason)."""
    n, regions = read_puzzle(puzzle_path)
    out = output.strip()
    if not expect_solution:
        if out == "NO SOLUTION":
            return True, "ok"
        return False, "this puzzle has no solution; expected exactly 'NO SOLUTION'"
    if out == "NO SOLUTION":
        return False, "this puzzle is solvable but you printed 'NO SOLUTION'"
    rows = [r.strip() for r in out.splitlines()]
    if len(rows) != n:
        return False, "expected %d output lines, got %d" % (n, len(rows))
    for i, r in enumerate(rows):
        if len(r) != n:
            return False, "output line %d has %d characters, expected %d" % (i + 1, len(r), n)
        if set(r) - set("*."):
            return False, "output line %d has characters other than '*' and '.'" % (i + 1)
    cats = [(r, c) for r in range(n) for c in range(n) if rows[r][c] == "*"]
    if len(cats) != n:
        return False, "expected %d cats, found %d" % (n, len(cats))
    if len({r for r, _ in cats}) != n:
        return False, "two cats share a row"
    if len({c for _, c in cats}) != n:
        return False, "two cats share a column"
    seen = {}
    for r, c in cats:
        k = regions[r][c]
        if k in seen:
            return False, "region %s has two cats: (%d,%d) and (%d,%d)" % (k, seen[k][0], seen[k][1], r, c)
        seen[k] = (r, c)
    for (a, b), (c, d) in combinations(cats, 2):
        if max(abs(a - c), abs(b - d)) <= 1:
            return False, "cats at (%d,%d) and (%d,%d) touch" % (a, b, c, d)
    return True, "ok"


# ---------------------------------------------------------------------------
# Build and run
# ---------------------------------------------------------------------------
def compile_sources(src_dir, build_dir):
    source = os.path.join(src_dir, "Meowdoku.java")
    if not os.path.exists(source):
        sys.exit("Meowdoku.java not found in %s" % os.path.abspath(src_dir))
    if not shutil.which("javac") or not shutil.which("java"):
        sys.exit("javac/java not found on your PATH. Install a JDK (17 or newer is fine).")
    r = subprocess.run(["javac", "-d", build_dir, source], capture_output=True, text=True)
    if r.returncode != 0:
        print("COMPILE ERROR\n" + r.stdout + r.stderr)
        sys.exit(1)


def run_one(build_dir, puzzle_path, timeout, stdin_text="", cwd=None):
    try:
        r = subprocess.run(["java", "-cp", build_dir, "Meowdoku", puzzle_path],
                           input=stdin_text, capture_output=True, text=True, timeout=timeout, cwd=cwd)
    except subprocess.TimeoutExpired:
        return None, "", "timed out after %s s" % timeout
    return r.stdout, r.stderr, ("exit code %d" % r.returncode if r.returncode else "")


def run_with_mode(build_dir, puzzle_path, mode, timeout):
    """Some public puzzles check *how* the file is passed, like the autograder does."""
    if mode == "spaces":      # absolute path with spaces, program started in another directory
        tmp = tempfile.mkdtemp(prefix="meowdoku_path_")
        try:
            folder = os.path.join(tmp, "my puzzles")
            os.makedirs(folder)
            copy = os.path.join(folder, "board one.txt")
            shutil.copyfile(puzzle_path, copy)
            other = os.path.join(tmp, "cwd")
            os.makedirs(other)
            return run_one(build_dir, copy, timeout, cwd=other)
        finally:
            shutil.rmtree(tmp, ignore_errors=True)
    if mode == "stdin":       # standard input holds junk; the puzzle must come from the file
        return run_one(build_dir, puzzle_path, timeout, stdin_text="this is not a puzzle\n3\nXYZ\n")
    return run_one(build_dir, puzzle_path, timeout)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--src", default=".", help="folder containing Meowdoku.java")
    ap.add_argument("--puzzles", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "puzzles"))
    ap.add_argument("--one", help="run a single puzzle file and print your output")
    ap.add_argument("--timeout", type=float, default=10.0)
    args = ap.parse_args()

    with open(os.path.join(args.puzzles, "manifest.json")) as f:
        manifest = {m["file"]: m for m in json.load(f)}

    build_dir = tempfile.mkdtemp(prefix="meowdoku_build_")
    try:
        compile_sources(args.src, build_dir)

        if args.one:
            name = os.path.basename(args.one)
            solvable = manifest[name]["solvable"] if name in manifest else True
            mode = manifest[name].get("mode", "plain") if name in manifest else "plain"
            out, err, note = run_with_mode(build_dir, os.path.abspath(args.one), mode, args.timeout)
            print("--- your stdout ---")
            print(out if out is not None else "(timed out)")
            if err.strip():
                print("--- your stderr ---\n" + err)
            if out is not None:
                ok, why = check_output(args.one, out, solvable)
                print("RESULT:", "PASS" if ok else "FAIL (%s)" % why)
            else:
                print("RESULT: FAIL (%s)" % note)
            return

        passed = 0
        for name in sorted(manifest):
            m = manifest[name]
            path = os.path.join(args.puzzles, name)
            out, err, note = run_with_mode(build_dir, os.path.abspath(path), m.get("mode", "plain"), args.timeout)
            if out is None:
                ok, why = False, note
            else:
                ok, why = check_output(path, out, m["solvable"])
                if not ok and note:
                    why += " (%s)" % note
            passed += ok
            print("%s  %-14s N=%-2d %s" % ("PASS" if ok else "FAIL", name, m["n"], "" if ok else "-> " + why))
            if not ok and err.strip():
                print("      stderr: " + err.strip().splitlines()[-1])
        print("\n%d / %d public puzzles passed" % (passed, len(manifest)))
        print("A few extra hidden puzzles (about 20% of the grade) are run on Gradescope. Test your own boards too.")
        sys.exit(0 if passed == len(manifest) else 1)
    finally:
        shutil.rmtree(build_dir, ignore_errors=True)


if __name__ == "__main__":
    main()
