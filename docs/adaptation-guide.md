# Как адаптировать проект под свою работу

Начните с fork репозитория и отдельной ветки. Меняйте по одному подключению, после каждой правки запускайте API-тесты или компиляцию UI.

## 1. Адреса стендов

Dev задан в `EnvironmentConfig`. Замените три константы:

```java
DEV_WEB_URL
DEV_AUTH_API_URL
DEV_MOVIES_API_URL
```

UAT получает адреса из переменных. Этот вариант подходит для GitLab Variables и Jenkins Credentials. Prod в учебном проекте заблокирован, чтобы случайный запуск не создавал данные у реальных пользователей.

## 2. DTO и builders

Сверьте JSON вашей системы с классами в `api-tests/src/main/java/ru/tqa/cicd/dto`.

Добавляйте DTO по ответу API, а не по форме на сайте. Для тела запроса используйте Lombok `@Builder`. Для ответа оставьте `@JsonIgnoreProperties(ignoreUnknown = true)`, если тесту нужна только часть полей.

После изменения DTO запустите:

```bash
./gradlew :api-tests:compileJava :api-tests:test
```

## 3. REST-клиенты

`AuthApiClient` отвечает за пользователя. `MoviesApiClient` работает с фильмами и отзывами. В тестах не собираются URL, заголовки и Bearer token вручную.

Для нового сервиса создайте отдельный client рядом с ними. Общую спецификацию запроса добавьте в `ApiSpecifications`. Не логируйте пароль, cookie и `Authorization`.

## 4. Подготовка тестовых данных

`UserFixture.create()` регистрирует пользователя через REST до открытия браузера. `UiTestBase` удаляет его после теста. `MovieUiTest` таким же способом создаёт и удаляет отзыв.

Для своей системы повторите этот приём:

1. Создайте данные через API в `@BeforeEach`
2. Выполните через UI только проверяемое действие
3. Удалите данные через API в `@AfterEach`

Так UI-тест тратит время на интерфейс, а не на заполнение служебных форм.

## 5. Page Object и локаторы

Page Object лежат в `ui-tests/src/test/java/ru/tqa/cicd/pages`. Замените пути страниц и `data-qa-id` на локаторы своего приложения.

Попросите frontend-разработчиков добавить стабильные тестовые атрибуты. CSS-классы из дизайна и XPath по тексту меняются чаще.

Page Object выполняет действия и проверки интерфейса. Создание данных, токены и вызовы REST остаются в fixtures и API-клиентах.

## 6. Selenoid

Версия Chrome указана в двух местах:

- `infra/selenoid/browsers.json`
- `scripts/run-ui-tests.sh`, `.gitlab-ci.yml` и `Jenkinsfile`

Меняйте тег одновременно во всех файлах. Затем скачайте образ и проверьте `/status`:

```bash
docker pull selenoid/vnc_chrome:128.0
docker compose -f infra/selenoid/docker-compose.yml up -d
curl --fail http://localhost:4444/status
```

На общем сервере задайте лимит сессий по памяти agent. Одна Chrome-сессия обычно требует сотни мегабайт.

## 7. GitLab CI

Скопируйте `.gitlab-ci.yml`, затем проверьте:

- разрешён ли privileged mode у Runner;
- совпадают ли пути Gradle-модулей;
- заведены ли protected variables UAT;
- сохраняются ли JUnit, Allure и скриншоты при `when: always`.

Подробная настройка: [GitLab CI](gitlab.md).

## 8. Jenkins

Скопируйте `Jenkinsfile`. Замените label `tqa-docker-java-21` на label своего agent. Создайте credentials с ID, указанными в pipeline, либо переименуйте ID в коде. Проверьте, что `SELENOID_PORT` и `SELENOID_UI_PORT` свободны на этом agent.

На agent проверьте команды от пользователя Jenkins:

```bash
java -version
docker version
docker compose version
```

Установите плагины JUnit и Allure. Pipeline публикует результаты из `**/build/test-results/test` и `**/build/allure-results`.

Подробная настройка: [Jenkins](jenkins.md).

## 9. Проверка перед push

```bash
./gradlew clean check
bash -n scripts/*.sh
./scripts/verify-no-secrets.sh
git diff --check
```

UI запускайте через `./scripts/run-ui-tests.sh` на машине с Docker. После падения заберите Gradle HTML, Allure results, скриншот и лог Selenoid.
