#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="$project_root/infra/local-ci/.env"
compose_file="$project_root/infra/local-ci/docker-compose.gitlab.yml"

if [[ ! -f "$env_file" ]]; then
  echo "Скопируйте infra/local-ci/.env.example в infra/local-ci/.env" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$env_file"
set +a

gitlab_password="${GITLAB_ROOT_PASSWORD:-}"
if [[ ${#gitlab_password} -lt 16 \
  || ! "$gitlab_password" =~ [[:lower:]] \
  || ! "$gitlab_password" =~ [[:upper:]] \
  || ! "$gitlab_password" =~ [[:digit:]] \
  || ! "$gitlab_password" =~ [^[:alnum:]] ]]; then
  echo "GITLAB_ROOT_PASSWORD: минимум 16 символов, верхний и нижний регистр, цифра и спецсимвол" >&2
  echo "Сгенерируйте значение командой: openssl rand -base64 24" >&2
  exit 1
fi

docker compose --env-file "$env_file" -f "$compose_file" up -d --wait
echo "GitLab готов: http://localhost:18081"
