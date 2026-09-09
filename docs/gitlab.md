# Запуск в GitLab CI

`.gitlab-ci.yml` проверяет код, запускает API-тесты, поднимает Selenoid для UI и собирает HTML-отчёт Allure. Dev запускается после каждого push. Headless включён через `JAVA_TOOL_OPTIONS`. UAT доступен вручную только из защищённой ветки; `allow_failure: true` делает этот job необязательным, поэтому ожидание ручного запуска не блокирует Dev pipeline.

Между API и UI выполняется `integration_tests`. GitLab Runner через Docker-in-Docker поднимает временные PostgreSQL, Kafka и Vault. `REQUIRE_DOCKER=true` запрещает тихо пропустить эти тесты в CI

Поведение необязательных ручных jobs описано в [документации GitLab](https://docs.gitlab.com/ci/jobs/job_control/).

## Runner

Integration и UI jobs используют Docker-in-Docker. Runner должен разрешать privileged containers. На общем Runner без изоляции не подключайте `/var/run/docker.sock`: job получит доступ ко всем контейнерам хоста.

Для собственного Runner добавьте в `config.toml`:

```toml
[[runners]]
  executor = "docker"
  [runners.docker]
    privileged = true
```

## Переменные UAT

В `Settings → CI/CD → Variables` создайте защищённые переменные:

- `UAT_WEB_URL`
- `UAT_AUTH_API_URL`
- `UAT_MOVIES_API_URL`

Значения нужны только job `uat_manual`. Адреса Dev уже лежат в OWNER properties. На Prod текущие fixtures не проходят вход без подтверждения почты; подробности и порядок запуска есть в [README](../README.md).

## Артефакты

После job откройте вкладку `Artifacts`. Там сохраняются JUnit XML, отчёты Gradle, `allure-results`, скриншоты Selenide и логи Selenoid. Job `allure_dev` запускается с `when: always` и добавляет HTML в `build/allure-report`, включая запуски с упавшими тестами. После скачивания распакуй архив и открой отчёт через HTTP-сервер по [инструкции в README](../README.md).
