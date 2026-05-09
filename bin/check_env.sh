#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

source "$ROOT_DIR/bin/_java.sh"
JAVA_CMD="$(detect_java)"
JAVAC_CMD="$(detect_javac)"

"$JAVA_CMD" -version
"$JAVAC_CMD" -version
echo "required runtime: JDK 17+, stdlib only"

model_url=""
model_name=""
api_key=""
if [[ -f .env ]]; then
  model_url="$(grep -E '^MODEL_CHAT_COMPLETIONS_URL=' .env | tail -n1 | cut -d= -f2- || true)"
  model_name="$(grep -E '^MODEL_NAME=' .env | tail -n1 | cut -d= -f2- || true)"
  api_key="$(grep -E '^MODEL_API_KEY=' .env | tail -n1 | cut -d= -f2- || true)"
fi

echo "model url configured: $([[ -n "$model_url" ]] && echo yes || echo no)"
echo "model url: ${model_url:-not configured}"
echo "model name: ${model_name:-not configured}"
echo "api key configured: $([[ -n "$api_key" ]] && echo yes || echo no)"
echo "environment looks usable"
