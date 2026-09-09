# PostgreSQL, Kafka, Awaitility и Vault

Модуль `integration-tests` содержит четыре примера:

- JDBC-запись, чтение и удаление строки в PostgreSQL
- отправка и чтение JSON-события напрямую через Kafka producer/consumer
- асинхронный сценарий «БД + Kafka» с ожиданием через Awaitility
- запись и чтение секрета через Vault KV v2 HTTP API

## Автоматический запуск через Testcontainers

Нужны Java 21 и запущенный Docker Engine. Тест сам поднимет временные PostgreSQL, Kafka и Vault, а после завершения Testcontainers удалит контейнеры

```bash
./gradlew :integration-tests:test
```

Если Docker не запущен, интеграционные сценарии будут пропущены. В Jenkins и GitLab Docker обязателен, поэтому pipeline проверяет, что тесты действительно выполнились

## Ручной запуск через Docker Compose

Сначала создай локальный файл с паролями

```bash
cp infra/integration/.env.example infra/integration/.env
```

Замени оба значения в `.env`. Файл исключён из Git

Подними сервисы

```bash
docker compose --env-file infra/integration/.env \
  -f infra/integration/docker-compose.yml up -d --wait postgres kafka vault

docker compose --env-file infra/integration/.env \
  -f infra/integration/docker-compose.yml run --rm vault-init
```

Проверь подключения

```bash
docker compose --env-file infra/integration/.env \
  -f infra/integration/docker-compose.yml ps

docker compose --env-file infra/integration/.env \
  -f infra/integration/docker-compose.yml exec kafka \
  kafka-topics --bootstrap-server localhost:29092 --list
```

Запусти тесты и автоматически останови сервисы

```bash
./scripts/run-integration-tests.sh
```

Проверить итоговый Compose-конфиг без запуска контейнеров:

```bash
./scripts/verify-integration-compose.sh
```

Volume PostgreSQL останется на машине. Удаляй его только когда данные больше не нужны

```bash
docker compose --env-file infra/integration/.env \
  -f infra/integration/docker-compose.yml down --volumes
```

## Где лежат настройки

Несекретные значения находятся в `api-tests/src/main/resources/config/dev.properties` и `uat.properties`. OWNER позволяет заменить их переменными окружения или system properties

Пароль PostgreSQL хранится в Vault по одному из путей:

- `secret/data/java-aqa/dev`
- `secret/data/java-aqa/uat`

Тест передаёт только `VAULT_ADDR` и `VAULT_TOKEN`, затем получает `database-password` через Vault API. Dev root token из локальной лабы нельзя использовать на рабочем стенде. Для рабочего Vault нужны AppRole, JWT/OIDC или Kubernetes Auth с минимальной политикой чтения
