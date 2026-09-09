#!/usr/bin/env sh
set -eu

# KV v2 хранит секреты по API-пути secret/data/<path>.
vault kv put -mount=secret java-aqa/dev database-password="$POSTGRES_PASSWORD"
vault kv put -mount=secret java-aqa/uat database-password="$POSTGRES_PASSWORD"
