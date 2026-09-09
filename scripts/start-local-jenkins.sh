#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="$project_root/infra/local-ci/.env"
compose_file="$project_root/infra/local-ci/docker-compose.jenkins.yml"

if [[ ! -f "$env_file" ]]; then
  echo "Скопируйте infra/local-ci/.env.example в infra/local-ci/.env" >&2
  exit 1
fi

docker compose --env-file "$env_file" -f "$compose_file" up -d --build --wait
echo "Jenkins готов: http://localhost:18080"
