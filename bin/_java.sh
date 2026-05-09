#!/usr/bin/env bash

detect_java() {
  if [[ -n "${JAVA_BIN:-}" ]]; then
    echo "$JAVA_BIN"
    return 0
  fi
  local candidates=(
    "${JAVA_HOME:-}/bin/java"
    "/opt/homebrew/opt/openjdk@17/bin/java"
    "/usr/local/opt/openjdk@17/bin/java"
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
  if command -v java >/dev/null 2>&1; then
    echo java
    return 0
  fi
  echo java
}

detect_javac() {
  if [[ -n "${JAVAC_BIN:-}" ]]; then
    echo "$JAVAC_BIN"
    return 0
  fi
  local candidates=(
    "${JAVA_HOME:-}/bin/javac"
    "/opt/homebrew/opt/openjdk@17/bin/javac"
    "/usr/local/opt/openjdk@17/bin/javac"
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
  if command -v javac >/dev/null 2>&1; then
    echo javac
    return 0
  fi
  echo javac
}

compile_demo() {
  local javac_cmd
  javac_cmd="$(detect_javac)"
  mkdir -p build/classes
  local sources_file
  sources_file="$(mktemp)"
  find source/src/main/java -name '*.java' | sort > "$sources_file"
  "$javac_cmd" --release 11 -encoding UTF-8 -d build/classes @"$sources_file"
  rm -f "$sources_file"
}
