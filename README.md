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

Удаление отзыва и пользователя выполняется в `@AfterEach`. Очистка использует авторизацию созданного пользователя; её ошибки нужно разбирать отдельно от результата самого теста.

## Пошаговый запуск на Dev

### 1. Подготовь инструменты и скачай проект

Нужны Git, JDK 21, Docker Engine с Docker Compose v2. Для общего Allure-отчёта ниже используются Node.js 22 с npm и Python 3. Команды рассчитаны на Bash/Zsh; в Windows запускай их в WSL2 с доступом к Docker.

Для UI и интеграционных тестов выдели Docker около 6 ГБ памяти. Локальному GitLab нужно около 8 ГБ; Jenkins и GitLab удобнее запускать по очереди. Первый запуск скачивает зависимости и Docker-образы.

```bash
git clone https://github.com/tolcheev/java-aqa-cicd-example.git
cd java-aqa-cicd-example
chmod +x gradlew scripts/*.sh
java -version
./gradlew --version
docker info
docker compose version
node --version
npm --version
python3 --version
```

Java и Gradle должны использовать JDK 21. При другой версии задай `JAVA_HOME` на каталог установленного JDK 21 и добавь его `bin` в `PATH`.

Если проект уже скачан, перейди в его каталог и выполни `git pull --ff-only`. Все дальнейшие команды выполняй из корня проекта.

### 2. Собери проект и скомпилируй тесты

```bash
./scripts/verify-no-secrets.sh
./gradlew assemble testClasses
```

Ожидаемый результат: `BUILD SUCCESSFUL`. Этот шаг собирает код и компилирует тесты, но ещё не выполняет их. Отдельно устанавливать Gradle не нужно: используется Wrapper из репозитория.

### 3. Выбери Dev и запусти API-тесты

```bash
export TEST_ENV=dev
unset UAT_WEB_URL UAT_AUTH_API_URL UAT_MOVIES_API_URL
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Dselenide.headless=true"
./gradlew :api-tests:test
```

Адреса Dev уже заданы в `api-tests/src/main/resources/config/dev.properties`. Дополнительные пароли для API/UI не нужны: тесты создают временных пользователей.

При проверке 9 сентября 2026 года в API-модуле прошли 13 тестов. HTML-отчёт Gradle: `api-tests/build/reports/tests/test/index.html`.

### 4. Запусти интеграционные тесты

```bash
REQUIRE_DOCKER=true ./gradlew :integration-tests:test
```

Testcontainers сам поднимает PostgreSQL, Kafka и Vault, затем удаляет свои контейнеры. `REQUIRE_DOCKER=true` не даёт пропустить интеграционные сценарии при недоступном Docker. В проверенном запуске прошли 6 тестов.

Для ручной работы с сервисами через Docker Compose есть отдельная [инструкция](docs/integration-tests.md). Перед запуском по ней замени пароли в `infra/integration/.env`.

### 5. Запусти UI в Chrome через Selenoid

```bash
./scripts/run-ui-tests.sh
```

Переменная `JAVA_TOOL_OPTIONS` из шага 3 включает headless. Скрипт скачивает `selenoid/vnc_chrome:128.0`, запускает Selenoid, ждёт его готовности и выполняет 2 UI-теста. Chrome работает внутри Docker. После тестов скрипт останавливает Selenoid.

Во время прогона Selenoid UI доступен по адресу <http://localhost:8090>. Скриншоты и HTML страницы при ошибках находятся в `ui-tests/build/selenide-reports`.

Обычный `./gradlew check` проверяет API, выполняет интеграционные тесты при доступном Docker и компилирует UI. Полный прогон с браузером требует явного шага выше.

### 6. Собери общий Allure-отчёт

После предыдущих шагов выполни команду даже в случае падения теста. Она читает сохранённые результаты и не запускает тесты повторно:

```bash
npx --yes --package=allure-commandline@2.30.0 allure generate \
  api-tests/build/allure-results \
  integration-tests/build/allure-results \
  ui-tests/build/allure-results \
  --output build/allure-report --clean
```

При успешном полном прогоне в отчёте будет 21 пройденный тест: 13 API, 6 интеграционных и 2 UI. Это результат проверенного набора на 9 сентября 2026 года; при добавлении тестов количество изменится.

Для просмотра запусти локальный HTTP-сервер:

```bash
python3 -m http.server 18082 --bind 127.0.0.1 --directory build/allure-report
```

Открой <http://localhost:18082>. Для остановки сервера нажми `Ctrl+C`. Не открывай `index.html` двойным кликом: браузер может заблокировать загрузку данных отчёта через `file://`.

Перед каждым новым запуском тестового модуля Gradle очищает его старые `allure-results`. Для актуального общего отчёта сначала прогони все модули. `clean` удаляет результаты, поэтому не вызывай его между тестами и сборкой Allure.

## Пошаговый запуск Jenkins

Локальная сборка Jenkins уже содержит Java 21, Docker CLI, плагины JUnit и Allure.

1. Создай настройки командой ниже. Существующий `.env` сохраняется.

   ```bash
   test -f infra/local-ci/.env || cp infra/local-ci/.env.example infra/local-ci/.env
   ```

2. В `infra/local-ci/.env` замени `JENKINS_ADMIN_PASSWORD`. `JENKINS_REPOSITORY_URL` должен указывать на репозиторий с твоими запушенными изменениями; по умолчанию используется этот проект и ветка `main`.
3. Запусти `./scripts/start-local-jenkins.sh`. Открой <http://localhost:18080> и войди с `JENKINS_ADMIN_ID` и паролем из `.env`.
4. Открой job `java-aqa-cicd-example`. Нажми `Build Now`; если доступно `Build with Parameters`, выбери `TEST_ENV=dev` и нажми `Build`.
5. В `Console Output` проверь завершение API, Integration и UI tests. `Jenkinsfile` включает headless и публикует Allure в блоке `post { always { ... } }`.
6. Открой завершённую сборку, затем `Test Result` и `Allure Report`. Зелёная сборка имеет статус `SUCCESS`; при проверке Dev прошёл 21 тест. ZIP отчёта доступен в артефактах сборки.

Для существующего Jenkins настрой agent с label `tqa-docker-java-21` по [инструкции](docs/jenkins.md). Особенности локального Docker socket и адресов контейнеров описаны [здесь](docs/local-jenkins.md).

## Пошаговый запуск GitLab CI

Для существующего GitLab переходи к созданию проекта и Runner. Локальную установку можно поднять так:

1. Создай `infra/local-ci/.env`, если его ещё нет, той же командой из раздела Jenkins. Получи случайный пароль через `openssl rand -base64 24` и запиши его в `GITLAB_ROOT_PASSWORD`. Скрипт требует минимум 16 символов, верхний и нижний регистр, цифру и спецсимвол.
2. Запусти `./scripts/start-local-gitlab.sh`. Первый запуск может занимать 5–15 минут. Открой <http://localhost:18081> и войди как `root` с заданным паролем.
3. Создай пустой проект `java-aqa-cicd-example` без README. В `Settings / CI/CD / Runners` создай project runner без тегов с разрешением `Run untagged jobs`.
4. Запиши выданный authentication token `glrt-...` в `GITLAB_RUNNER_TOKEN` файла `.env`. Выполни `./scripts/register-local-gitlab-runner.sh`. Убедись, что Runner online. Для собственного Runner нужен Docker executor с `privileged = true`.
5. Создай Personal Access Token с правом `write_repository`, затем отправь код. Введи `root` как логин, а PAT как пароль. Токен в URL не добавляй.

   ```bash
   git remote add local-gitlab http://localhost:18081/root/java-aqa-cicd-example.git
   git push local-gitlab main
   ```

   Если remote уже существует, проверь его через `git remote get-url local-gitlab`. Для удалённого GitLab используй URL клонирования своего проекта.

6. Открой `Build / Pipelines`. Дождись успешного завершения `validate`, `api_tests_dev`, `integration_tests`, `ui_tests_dev` и `allure_dev`. Headless включён в `.gitlab-ci.yml`.
7. Открой job `allure_dev` и скачай `Artifacts`. Распакуй архив: отчёт находится в `build/allure-report`. Из каталога распакованного архива запусти HTTP-сервер командой из шага 6 локального прогона и открой <http://localhost:18082>.

`allure_dev` запускается с `when: always`, поэтому отчёт собирается и после падения тестов. Ошибка теста делает Dev pipeline красным; Allure показывает причину. Ручной `uat_manual` необязателен и не блокирует зелёный Dev pipeline на защищённой ветке. Артефакты GitLab хранятся 14 дней.

Подробности: [Runner и переменные](docs/gitlab.md), [локальная GitLab-лаба](docs/local-gitlab.md).

## UAT и проверка на Prod

Для учебного зелёного прогона используй `TEST_ENV=dev`. Код допускает только имена окружений `dev` и `uat`; `TEST_ENV=prod` отклоняется. Это проверка имени окружения, а не защита от подстановки production URL в переменные UAT.

Для отдельного UAT нужны `UAT_WEB_URL`, `UAT_AUTH_API_URL` и `UAT_MOVIES_API_URL`. В Jenkins они хранятся в Secret text credentials с ID `tqa-uat-web-url`, `tqa-uat-auth-api-url`, `tqa-uat-movies-api-url`. В GitLab создай одноимённые защищённые variables `UAT_*`; ручной job появляется только на защищённой ветке.

При проверке 9 сентября 2026 года Prod использовался как UAT. После регистрации пользователь требовал подтверждения почты, вход возвращал `403`. Текущие fixtures рассчитаны на автоматическое подтверждение в Dev. Полный UAT pipeline не проверен: для него сначала нужно адаптировать создание и удаление пользователей к подтверждению почты. Одной замены URL недостаточно.

## Остановка и очистка Docker

Останавливай только те локальные CI-сервисы, которые запускал:

```bash
docker compose --env-file infra/local-ci/.env \
  -f infra/local-ci/docker-compose.jenkins.yml down

docker compose --env-file infra/local-ci/.env \
  -f infra/local-ci/docker-compose.gitlab.yml down
```

Для очистки неиспользуемых образов и кэша:

```bash
docker system df
docker system prune -a --force
docker system df
```

`prune` затрагивает весь выбранный Docker daemon: удаляет остановленные контейнеры и неиспользуемые образы, в том числе от других проектов. Volumes эти команды сохраняют. Не добавляй `--volumes`: в них находятся настройки Jenkins, репозитории GitLab и данные локальных сервисов.

## Как перенести на рабочий проект

Порядок замены URL, DTO, клиентов, Page Object и CI-настроек описан в [руководстве по адаптации](docs/adaptation-guide.md).

## Безопасность

`scripts/verify-no-secrets.sh` ищет приватные ключи, JWT, присвоенные токены, настоящие `.env` и адрес Prod. Тестовый пароль из `TestUserFactory` не является credential и создаёт только временных пользователей Cinescope Dev.

Properties содержат адреса, имена ресурсов и пути Vault. Пароли находятся в Vault. Pipeline получает только bootstrap-доступ через защищённую CI variable или Jenkins Credential, затем читает нужный секрет во время теста.

Docker socket даёт контейнеру доступ к Docker-хосту. Используйте его на отдельном Jenkins agent или в изолированном GitLab Runner.
