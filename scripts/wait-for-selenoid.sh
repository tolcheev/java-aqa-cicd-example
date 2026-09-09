#!/usr/bin/env bash
set -euo pipefail

status_url="${SELENOID_STATUS_URL:-http://localhost:4444/status}"
attempts="${SELENOID_WAIT_ATTEMPTS:-30}"
delay_seconds="${SELENOID_WAIT_DELAY_SECONDS:-2}"

for ((attempt = 1; attempt <= attempts; attempt++)); do
  if curl --fail --silent --show-error "$status_url" >/dev/null; then
    echo "Selenoid готов: $status_url"
    exit 0
  fi
  sleep "$delay_seconds"
done

echo "Selenoid не ответил за $((attempts * delay_seconds)) секунд: $status_url" >&2
exit 1
