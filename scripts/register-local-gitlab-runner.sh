#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="$project_root/infra/local-ci/.env"
compose_file="$project_root/infra/local-ci/docker-compose.gitlab.yml"

if [[ ! -f "$env_file" ]]; then
  echo "Не найден infra/local-ci/.env" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$env_file"
set +a

if [[ ! "${GITLAB_RUNNER_TOKEN:-}" =~ ^glrt- ]]; then
  echo "Создайте project runner в GitLab и запишите glrt-токен в GITLAB_RUNNER_TOKEN" >&2
  exit 1
fi

docker compose --env-file "$env_file" -f "$compose_file" exec -T gitlab-runner \
  gitlab-runner register \
    --non-interactive \
    --url "http://gitlab" \
    --clone-url "http://gitlab" \
    --token "$GITLAB_RUNNER_TOKEN" \
    --executor "docker" \
    --docker-image "eclipse-temurin:21-jdk" \
    --docker-privileged \
    --docker-network-mode "java-aqa-local-gitlab_default" \
    --description "local-java-aqa-runner"

echo "Runner зарегистрирован"
