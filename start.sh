#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

detect_java() {
  if [[ -n "${JAVA_BIN:-}" ]]; then
    echo "$JAVA_BIN"
    return 0
  fi
  local candidates=(
    "${JAVA_HOME:-}/bin/java"
    "/opt/homebrew/opt/openjdk@21/bin/java"
    "/usr/local/opt/openjdk@21/bin/java"
    "/opt/homebrew/opt/openjdk/bin/java"
    "/usr/local/opt/openjdk/bin/java"
  )
  local candidate
  for candidate in "${candidates[@]}"; do
    if [[ -x "$candidate" ]]; then
      echo "$candidate"
      return 0
    fi
  done
  command -v java >/dev/null 2>&1 && echo java || echo java
}

detect_javac() {
  if [[ -n "${JAVAC_BIN:-}" ]]; then
    echo "$JAVAC_BIN"
    return 0
  fi
  local candidates=(
    "${JAVA_HOME:-}/bin/javac"
    "/opt/homebrew/opt/openjdk@21/bin/javac"
    "/usr/local/opt/openjdk@21/bin/javac"
    "/opt/homebrew/opt/openjdk/bin/javac"
    "/usr/local/opt/openjdk/bin/javac"
  )
  local candidate
  for candidate in "${candidates[@]}"; do
    if [[ -x "$candidate" ]]; then
      echo "$candidate"
      return 0
    fi
  done
  command -v javac >/dev/null 2>&1 && echo javac || echo javac
}

java_major_version() {
  local command_path="$1"
  local version_line
  version_line="$("$command_path" -version 2>&1 | head -n1 || true)"
  local version=""
  if [[ "$version_line" =~ \"([0-9][^\"]*)\" ]]; then
    version="${BASH_REMATCH[1]}"
  elif [[ "$version_line" =~ ([0-9]+(\.[0-9]+)*) ]]; then
    version="${BASH_REMATCH[1]}"
  fi
  local major
  case "$version" in
    1.*) major="$(printf '%s' "$version" | cut -d. -f2)" ;;
    *) major="$(printf '%s' "$version" | cut -d. -f1)" ;;
  esac
  [[ "$major" =~ ^[0-9]+$ ]] && echo "$major" || echo 0
}

require_jdk_21() {
  local java_cmd javac_cmd java_major javac_major
  java_cmd="$(detect_java)"
  javac_cmd="$(detect_javac)"
  java_major="$(java_major_version "$java_cmd")"
  javac_major="$(java_major_version "$javac_cmd")"
  if (( java_major < 21 || javac_major < 21 )); then
    echo "error: Java demo requires JDK 21 or newer." >&2
    echo "detected java:  $("$java_cmd" -version 2>&1 | head -n1)" >&2
    echo "detected javac: $("$javac_cmd" -version 2>&1 | head -n1)" >&2
    exit 1
  fi
}

compile_demo() {
  local javac_cmd sources_file
  javac_cmd="$(detect_javac)"
  require_jdk_21
  mkdir -p build/classes
  sources_file="$(mktemp)"
  find source/src/main/java -name '*.java' | sort > "$sources_file"
  "$javac_cmd" --release 21 -encoding UTF-8 -d build/classes @"$sources_file"
  rm -f "$sources_file"
}

QUESTION_PATH="${QUESTION_PATH:-${1:-source/examples/questions.json}}"
RESULT_PATH="${RESULT_PATH:-${2:-source/outputs/result.json}}"

compile_demo
JAVA_CMD="$(detect_java)"

"$JAVA_CMD" -cp build/classes com.contestdemo.Main \
  --question "$QUESTION_PATH" \
  --output "$RESULT_PATH"

"$JAVA_CMD" -cp build/classes com.contestdemo.tools.ShowAnswers "$RESULT_PATH"
