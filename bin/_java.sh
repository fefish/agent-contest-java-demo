#!/usr/bin/env bash

detect_java() {
  if [[ -n "${JAVA_BIN:-}" ]]; then
    echo "$JAVA_BIN"
    return 0
  fi
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
  "$javac_cmd" -encoding UTF-8 -d build/classes @"$sources_file"
  rm -f "$sources_file"
}
