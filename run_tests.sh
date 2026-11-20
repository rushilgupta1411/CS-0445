#!/usr/bin/env bash
# PittGuard test runner.
#
#   bash run_tests.sh                 run the VISIBLE tests against ./PittGuard.java
#   bash run_tests.sh --all           run visible + hidden tests (instructor)
#   bash run_tests.sh --only campus   run only tests whose name contains "campus"
#   bash run_tests.sh --dir some/dir  use some/dir/PittGuard.java instead of ./PittGuard.java
#   bash run_tests.sh --show          print a diff for every failing test
#
# A test passes when stdout matches tests/expected/<name>.out exactly and the exit code is 0.
# For tests whose expected file says ERROR, it passes when the exit code is non-zero,
# stderr is non-empty (and is not a JVM/uncaught-exception crash) and stdout is empty.
# (The real grader checks the "backbone:" line and the "best:" line in separate tests.)

cd "$(dirname "$0")" || exit 1
RUN_ALL=0; SHOW=0; ONLY=""; SRC_DIR="."
while [[ $# -gt 0 ]]; do
  case "$1" in
    --all) RUN_ALL=1 ;;
    --show) SHOW=1 ;;
    --only) ONLY="$2"; shift ;;
    --dir) SRC_DIR="$2"; shift ;;
    *) echo "unknown option $1"; exit 2 ;;
  esac
  shift
done

BUILD="$(mktemp -d)"; trap 'rm -rf "$BUILD"' EXIT
echo "Compiling $SRC_DIR/PittGuard.java ..."
if command -v javac >/dev/null 2>&1; then
  javac -d "$BUILD" "$SRC_DIR/PittGuard.java" || { echo "COMPILE ERROR"; exit 1; }
else
  java -m jdk.compiler/com.sun.tools.javac.Main -d "$BUILD" "$SRC_DIR/PittGuard.java" || { echo "COMPILE ERROR"; exit 1; }
fi

TIMEOUT=""; command -v timeout >/dev/null 2>&1 && TIMEOUT="timeout 10"
pass=0; fail=0
while IFS=$'\t' read -r name vis args; do
  [[ -z "$name" || "$name" == \#* ]] && continue
  [[ $RUN_ALL -eq 0 && "$vis" != "visible" ]] && continue
  [[ -n "$ONLY" && "$name" != *"$ONLY"* ]] && continue
  expected="tests/expected/$name.out"
  $TIMEOUT java -cp "$BUILD" PittGuard $args >"$BUILD/out" 2>"$BUILD/err"; code=$?
  tr -d '\r' < "$BUILD/out" > "$BUILD/out.clean"
  ok=1
  if [[ "$(head -c 5 "$expected")" == "ERROR" ]]; then
    [[ $code -ne 0 && -s "$BUILD/err" && ! -s "$BUILD/out.clean" ]] || ok=0
    # a crash (uncaught exception) or a missing class is not "rejecting the input"
    grep -qE "Could not find or load main class|ClassNotFoundException|NoClassDefFoundError|Main method not found|Exception in thread" "$BUILD/err" && ok=0
  else
    [[ $code -eq 0 ]] && cmp -s "$BUILD/out.clean" "$expected" || ok=0
  fi
  if [[ $ok -eq 1 ]]; then
    pass=$((pass+1)); echo "PASS  $name"
  else
    fail=$((fail+1)); echo "FAIL  $name   (exit $code)"
    if [[ $SHOW -eq 1 ]]; then
      echo "  command: java PittGuard $args"
      diff <(head -c 600 "$expected") <(head -c 600 "$BUILD/out.clean") | head -8 | sed 's/^/  /'
      [[ -s "$BUILD/err" ]] && { echo "  stderr:"; head -3 "$BUILD/err" | sed 's/^/    /'; }
    fi
  fi
done < tests/cases.tsv
echo
echo "Passed $pass / $((pass+fail))"
[[ $fail -eq 0 ]]
