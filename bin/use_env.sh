#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "usage: bash bin/use_env.sh <profile>"
  echo "example: bash bin/use_env.sh fuyao"
  exit 1
fi

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

PROFILE="$1"
PROFILE_FILE=".env.${PROFILE}"

if [[ ! -f "$PROFILE_FILE" ]]; then
  echo "profile not found: $PROFILE_FILE"
  exit 1
fi

cp "$PROFILE_FILE" .env
echo "active env profile: $PROFILE"
