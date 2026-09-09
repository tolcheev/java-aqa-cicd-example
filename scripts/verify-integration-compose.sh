#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
compose_file="$project_root/infra/integration/docker-compose.yml"

compose_config="$({
  POSTGRES_PASSWORD=compose-check-db \
  VAULT_DEV_ROOT_TOKEN_ID=compose-check-vault \
    docker compose -f "$compose_file" config
})"

require_config() {
  local expected="$1"

  if ! grep -Fq "$expected" <<<"$compose_config"; then
    echo "В итоговом Compose-конфиге нет: $expected" >&2
    exit 1
  fi
}

require_config "KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka:29092,EXTERNAL://localhost:19092"
require_config "KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL"
require_config "kafka-topics --bootstrap-server localhost:29092 --list"
require_config "VAULT_ADDR: http://127.0.0.1:8200"
