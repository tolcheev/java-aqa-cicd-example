# Запуск в GitLab CI

`.gitlab-ci.yml` проверяет код, запускает API-тесты, поднимает Selenoid для UI и собирает HTML-отчёт Allure. Dev запускается после каждого push. UAT доступен вручную только из защищённой ветки.

## Runner

UI job использует Docker-in-Docker. Runner должен разрешать privileged containers. На общем Runner без изоляции не подключайте `/var/run/docker.sock`: job получит доступ ко всем контейнерам хоста.

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

Значения нужны только job `uat_manual`. Адреса Dev уже лежат в `EnvironmentConfig`.

## Артефакты

После job откройте вкладку `Artifacts`. Там сохраняются JUnit XML, отчёты Gradle, `allure-results`, скриншоты Selenide и логи Selenoid. Job `allure_dev` добавляет готовый HTML в `build/allure-report`.
