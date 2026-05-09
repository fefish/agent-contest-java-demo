#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

source "$ROOT_DIR/bin/_java.sh"
compile_demo
JAVA_CMD="$(detect_java)"

"$JAVA_CMD" -cp build/classes com.contestdemo.tools.PrepareQuestions \
  source/examples/official_tasks.json \
  source/examples/questions.json

"$JAVA_CMD" -cp build/classes com.contestdemo.Main \
  --question source/examples/questions.json \
  --output source/outputs/result.json

"$JAVA_CMD" -cp build/classes com.contestdemo.tools.ShowAnswers source/outputs/result.json
