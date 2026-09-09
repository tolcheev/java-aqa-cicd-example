#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="$project_root/infra/local-ci/.env"
compose_file="$project_root/infra/local-ci/docker-compose.gitlab.yml"

if [[ ! -f "$env_file" ]]; then
  echo "Скопируйте infra/local-ci/.env.example в infra/local-ci/.env" >&2
  exit 1
fi

docker compose --env-file "$env_file" -f "$compose_file" up -d --wait
echo "GitLab готов: http://localhost:18081"
