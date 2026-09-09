#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
compose_file="$project_root/infra/integration/docker-compose.yml"
env_file="$project_root/infra/integration/.env"

if [[ ! -f "$env_file" ]]; then
  echo "Скопируйте infra/integration/.env.example в infra/integration/.env" >&2
  exit 1
fi

cleanup() {
  docker compose --env-file "$env_file" -f "$compose_file" down
}
trap cleanup EXIT

set -a
# shellcheck disable=SC1090
source "$env_file"
set +a

docker compose --env-file "$env_file" -f "$compose_file" up -d --wait postgres kafka vault
docker compose --env-file "$env_file" -f "$compose_file" run --rm vault-init

USE_EXTERNAL_INFRA=true \
DATABASE_JDBC_URL="jdbc:postgresql://localhost:15432/aqa" \
DATABASE_USERNAME="aqa" \
  KAFKA_BOOTSTRAP_SERVERS="localhost:19092" \
  VAULT_ADDR="http://localhost:18200" \
  VAULT_TOKEN="$VAULT_DEV_ROOT_TOKEN_ID" \
  "$project_root/gradlew" -p "$project_root" :integration-tests:test --rerun-tasks
