#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

source "$ROOT_DIR/bin/_java.sh"
compile_demo
JAVA_CMD="$(detect_java)"

SOURCE_PATH="${1:-source/examples/official_tasks.json}"
OUTPUT_PATH="${2:-source/examples/questions.json}"

"$JAVA_CMD" -cp build/classes com.contestdemo.tools.PrepareQuestions "$SOURCE_PATH" "$OUTPUT_PATH"
