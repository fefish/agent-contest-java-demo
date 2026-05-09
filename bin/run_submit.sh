#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

source "$ROOT_DIR/bin/_java.sh"
compile_demo
JAVA_CMD="$(detect_java)"

QUESTION_PATH="${1:-source/examples/questions.json}"
OUTPUT_PATH="${2:-source/outputs/submission_result.json}"

"$JAVA_CMD" -cp build/classes com.contestdemo.Main \
  --question "$QUESTION_PATH" \
  --output "$OUTPUT_PATH"
