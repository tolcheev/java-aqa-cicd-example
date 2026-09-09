#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
compose_file="$project_root/infra/selenoid/docker-compose.yml"

cleanup() {
  docker compose -f "$compose_file" down
}
trap cleanup EXIT

# Версию браузера меняйте одновременно в browsers.json и команде pull.
docker pull selenoid/vnc_chrome:128.0
docker compose -f "$compose_file" up -d
"$project_root/scripts/wait-for-selenoid.sh"

SELENOID_URL="${SELENOID_URL:-http://localhost:4444/wd/hub}" \
  "$project_root/gradlew" -p "$project_root" :ui-tests:test
