# Интеграционные примеры: Kafka, PostgreSQL, Vault и OWNER

## Цель

Дополнить учебный Java AQA-проект воспроизводимыми примерами работы с асинхронными событиями, базой данных и секретами. Один и тот же код должен запускаться автоматически через Testcontainers и вручную против инфраструктуры из Docker Compose.

## Структура

В проект добавляется модуль `integration-tests`. Он содержит JDBC-клиент PostgreSQL, Kafka producer/consumer, пример сервиса с асинхронной публикацией события и клиент HashiCorp Vault. В `api-tests` DataFaker заменяет ручную генерацию части тестовых данных.

Конфигурация переносится на OWNER. Файлы `dev.properties` и `uat.properties` содержат только несекретные адреса и имена ресурсов. Значения из system properties и переменных окружения имеют приоритет. Production остаётся запрещённым.

## PostgreSQL

Testcontainers поднимает временный PostgreSQL. Миграция создаёт таблицу тестовых заказов. Тест подключается через JDBC, записывает строку, читает её обратно и удаляет после проверки.

Docker Compose поднимает тот же PostgreSQL для ручного запуска. Адрес подключения передаётся через OWNER-настройки, поэтому тестовый код не меняется.

## Kafka и асинхронность

Первый сценарий напрямую создаёт Kafka producer и consumer, отправляет событие и проверяет полученное сообщение.

Второй сценарий вызывает тестовый сервис. Сервис сохраняет данные в PostgreSQL и асинхронно публикует событие в Kafka. Тест использует Awaitility: ждёт появления записи и события без `Thread.sleep`.

Сообщения сериализуются в JSON DTO. Для каждого запуска используются уникальные topic, key и данные из DataFaker.

## Vault и секреты

Testcontainers и Docker Compose поднимают HashiCorp Vault в dev-режиме только для учебного окружения. Bootstrap-скрипт записывает пример секрета в разные KV v2 пути для `dev` и `uat`.

`VaultSecretProvider` подключается к Vault по `VAULT_ADDR`, получает путь из OWNER-конфигурации и читает секрет через HTTP API. Токен передаётся только через `VAULT_TOKEN`; в properties и Git он не попадает. Интеграционный тест записывает временный секрет, читает его через provider и проверяет значение.

В Jenkins и GitLab хранится только bootstrap-доступ к Vault. Прикладные логины и пароли загружаются во время pipeline. В документации отдельно отмечается, что dev-token подходит только для локальной практики, а рабочим проектам нужны AppRole, JWT/OIDC или Kubernetes Auth.

## Режимы запуска

- `./gradlew :integration-tests:test` запускает PostgreSQL, Kafka и Vault через Testcontainers
- `docker compose -f infra/integration/docker-compose.yml up -d` поднимает инфраструктуру вручную
- `USE_EXTERNAL_INFRA=true ./gradlew :integration-tests:test` подключает тесты к Compose вместо Testcontainers

Jenkins и GitLab используют автоматический режим Testcontainers. Артефакты JUnit и Allure сохраняются вместе с результатами остальных модулей.

## Проверка

Разработка идёт через красный и зелёный циклы для OWNER-конфигурации, JDBC, Kafka, асинхронного сценария и Vault. Финальная проверка включает все Gradle-тесты, сборку Allure, разбор YAML/JSON, проверку shell-скриптов и поиск секретов.
