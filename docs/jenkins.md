# Запуск в Jenkins

Создайте Pipeline job и выберите `Pipeline script from SCM`. Укажите URL своего репозитория, ветку и путь `Jenkinsfile`. Jenkins сам прочитает этапы из проекта.

## Agent

Pipeline ищет agent с label `docker-java-21`. На нём нужны:

- JDK 21
- Docker Engine и Docker Compose v2
- доступ пользователя Jenkins к Docker
- плагины JUnit и Allure

Label меняется в первой строке блока `agent` в `Jenkinsfile`.

## UAT

Для запуска UAT добавьте три Secret text в `Manage Jenkins → Credentials`:

- `tqa-uat-web-url`
- `tqa-uat-auth-api-url`
- `tqa-uat-movies-api-url`

Выберите `uat` в параметре `TEST_ENV`. Pipeline подставит адреса только на время тестовых стадий. Для Dev эти credentials не нужны.

## Результат

JUnit показывает тесты прямо в build. Allure строится из каталогов обоих модулей. При падении UI остаются HTML-отчёт Gradle, скриншот, page source и лог браузерной сессии.
