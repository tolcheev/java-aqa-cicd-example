# Запуск в Jenkins

Создайте Pipeline job и выберите `Pipeline script from SCM`. Укажите URL своего репозитория, ветку и путь `Jenkinsfile`. Jenkins сам прочитает этапы из проекта.

## Agent

Pipeline ищет отдельный agent с label `tqa-docker-java-21`. На нём нужны:

- JDK 21
- Docker Engine и Docker Compose v2
- доступ пользователя Jenkins к Docker
- плагины JUnit и Allure

Label меняется в первой строке блока `agent` в `Jenkinsfile`. Для T-QA зарезервированы порты 4445 и 8091, поэтому job не занимает стандартный порт Jenkins 8080 и Selenoid 4444.

Стадия `Integration tests` запускает PostgreSQL, Kafka и Vault через Testcontainers. На agent должен быть доступен Docker socket. Переменная `REQUIRE_DOCKER=true` делает отсутствие Docker ошибкой сборки

## UAT

Для запуска UAT добавьте три Secret text в `Manage Jenkins → Credentials`:

- `tqa-uat-web-url`
- `tqa-uat-auth-api-url`
- `tqa-uat-movies-api-url`

Выберите `uat` в параметре `TEST_ENV`. Pipeline подставит адреса только на время тестовых стадий. Для Dev эти credentials не нужны.

## Результат

JUnit показывает тесты прямо в build. Allure строится из каталогов трёх модулей. При падении UI остаются HTML-отчёт Gradle, скриншот, page source и лог браузерной сессии.
