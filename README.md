# Java AQA CI/CD: Cinescope

Учебный проект показывает один набор автотестов в двух CI-системах. API-тесты работают через REST Assured, UI-тесты через Selenide и Selenoid. Интеграционный модуль показывает работу с PostgreSQL, Kafka и Vault. Объект тестирования UI/API: Cinescope Dev.

## Стек

- Java 21 и Gradle 8.14.3
- JUnit 5, AssertJ и Lombok
- OWNER и DataFaker
- REST Assured 6.0.1
- Selenide 7.18.1 и Selenoid 1.11.3
- Awaitility, PostgreSQL JDBC и Apache Kafka clients
- Testcontainers и HashiCorp Vault KV v2
- Allure Report
- GitLab CI и Jenkins Pipeline

Версии закреплены в `build.gradle`, Gradle Wrapper и Docker-конфигурации.

## Структура

```text
api-tests/
  src/main/java/     DTO, builders, EnvironmentConfig и REST-клиенты
  src/test/java/     API-сценарии и проверки конфигурации
ui-tests/
  src/test/java/     Page Object, REST fixtures и UI-сценарии
integration-tests/
  src/test/java/     JDBC, Kafka, Awaitility и Vault
infra/integration/   PostgreSQL, Kafka и Vault для ручного запуска
infra/local-ci/      локальные Jenkins, GitLab CE и GitLab Runner
infra/selenoid/      Selenoid, Selenoid UI и версия Chrome
scripts/             запуск UI, ожидание Selenoid, проверка секретов
.gitlab-ci.yml       pipeline GitLab
Jenkinsfile          pipeline Jenkins
```

`ui-tests` зависит от `api-tests`. Поэтому UI-сценарий создаёт пользователя и отзыв теми же REST-клиентами, которые проверяются в API-модуле. Дублировать DTO и авторизацию не нужно.

`integration-tests` также использует общую OWNER-конфигурацию. DataFaker создаёт уникальные данные, JDBC проверяет PostgreSQL, Kafka client отправляет и читает события, а Awaitility ждёт асинхронный результат без `Thread.sleep`.

## Пирамида тестов

API-модуль проверяет конфигурацию окружений, регистрацию, логин, негативные сценарии и чтение фильмов. Отдельный lifecycle test создаёт и удаляет отзыв.

UI-модуль содержит два сквозных сценария:

- пользователь, созданный через API, входит через форму;
- отзыв, созданный в `@BeforeEach` через API, отображается в карточке фильма.

Удаление отзыва и пользователя выполняется в `@AfterEach`. Тестовые данные не остаются на Dev после зелёного или упавшего теста.

## Требования

Для API нужен JDK 21. Для UI и интеграционных тестов дополнительно нужны Docker Engine, Docker Compose v2 и 6 ГБ свободной памяти. Локальный GitLab требует около 8 ГБ. Jenkins и GitLab вместе лучше запускать при 12 ГБ свободной памяти.

Проверьте Java:

```bash
java -version
./gradlew --version
```

## Запуск API

Dev настроен по умолчанию:

```bash
./gradlew :api-tests:test
```

HTML-отчёт JUnit появится в `api-tests/build/reports/tests/test`.

## PostgreSQL, Kafka и Vault

Testcontainers сам поднимает временную инфраструктуру:

```bash
./gradlew :integration-tests:test
```

Для ручного подключения используется Docker Compose:

```bash
cp infra/integration/.env.example infra/integration/.env
./scripts/run-integration-tests.sh
```

Полный разбор: [PostgreSQL, Kafka, Awaitility и Vault](docs/integration-tests.md).

## Запуск UI через Selenoid

Скрипт скачает закреплённый образ Chrome, поднимет Selenoid, запустит два UI-теста и остановит контейнеры:

```bash
./scripts/run-ui-tests.sh
```

Selenoid UI во время запуска доступен на `http://localhost:8090`. Скриншоты и page source сохраняются в `ui-tests/build/selenide-reports`.

Локальный Chrome можно включить явно:

```bash
./gradlew :ui-tests:test -Dremote=false
```

Обычный `./gradlew check` запускает API-тесты, интеграционные тесты при доступном Docker и компилирует UI-suite без браузера.

## UAT

Prod намеренно заблокирован в `EnvironmentConfig`. OWNER читает Dev/UAT из `config/dev.properties` и `config/uat.properties`. Переменные окружения и system properties перекрывают файлы. Для UAT нужны три адреса:

```bash
export TEST_ENV=uat
export UAT_WEB_URL=https://uat.example.test
export UAT_AUTH_API_URL=https://auth.uat.example.test
export UAT_MOVIES_API_URL=https://api.uat.example.test

./gradlew :api-tests:test
./scripts/run-ui-tests.sh
```

Скопируйте `.env.example` только как подсказку. Файл `.env` исключён из Git.

## Allure

Сначала запустите тесты, затем соберите отчёт нужного модуля:

```bash
./gradlew :api-tests:test :api-tests:allureReport
./gradlew :integration-tests:test :integration-tests:allureReport
./gradlew :ui-tests:test :ui-tests:allureReport
```

В CI `allure-results` сохраняются даже после падения. GitLab дополнительно собирает HTML. Jenkins публикует отчёт плагином Allure.

## CI/CD

- [GitLab CI](docs/gitlab.md): Runner, protected variables, artifacts
- [Jenkins](docs/jenkins.md): Pipeline from SCM, agent label, credentials
- [Локальный GitLab CE и Runner](docs/local-gitlab.md)
- [Локальный Jenkins](docs/local-jenkins.md)

Оба pipeline читаются из файлов репозитория. Изменение `Jenkinsfile` или `.gitlab-ci.yml` проходит обычный code review вместе с тестами.

## Как перенести на рабочий проект

Порядок замены URL, DTO, клиентов, Page Object и CI-настроек описан в [руководстве по адаптации](docs/adaptation-guide.md).

## Безопасность

`scripts/verify-no-secrets.sh` ищет приватные ключи, JWT, присвоенные токены, настоящие `.env` и адрес Prod. Тестовый пароль из `TestUserFactory` не является credential и создаёт только временных пользователей Cinescope Dev.

Properties содержат адреса, имена ресурсов и пути Vault. Пароли находятся в Vault. Pipeline получает только bootstrap-доступ через защищённую CI variable или Jenkins Credential, затем читает нужный секрет во время теста.

Docker socket даёт контейнеру доступ к Docker-хосту. Используйте его на отдельном Jenkins agent или в изолированном GitLab Runner.
