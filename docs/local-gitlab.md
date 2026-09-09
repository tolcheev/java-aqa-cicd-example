# Локальная GitLab CI-лаба

Лаба поднимает GitLab Community Edition и GitLab Runner. Первый запуск занимает 5–15 минут и требует около 8 ГБ свободной памяти

Если машине не хватает 12 ГБ свободной памяти, не запускай GitLab и Jenkins одновременно

## Запуск GitLab

Создай локальный файл настроек

```bash
cp infra/local-ci/.env.example infra/local-ci/.env
```

Сгенерируй пароль:

```bash
openssl rand -base64 24
```

Запиши результат в `GITLAB_ROOT_PASSWORD`. GitLab 19 отклоняет пароли из обычных слов и прерывает первый запуск

После этого запусти GitLab

```bash
./scripts/start-local-gitlab.sh
```

Открой `http://localhost:18081` и войди под пользователем `root` с паролем из `.env`

## Проект и Runner

1. Нажми `New project` → `Create blank project`
2. Назови проект `java-aqa-cicd-example`
3. Не создавай README: в локальный GitLab будет отправлена готовая история Git
4. Открой `Settings` → `CI/CD` → `Runners`
5. Создай project runner без тегов и скопируй authentication token с префиксом `glrt-`
6. Запиши token в `GITLAB_RUNNER_TOKEN` внутри локального `.env`
7. Зарегистрируй Runner

```bash
./scripts/register-local-gitlab-runner.sh
```

Runner должен появиться в проекте со статусом online

## Push и первый pipeline

Создай Personal Access Token с правом `write_repository`. Не добавляй его в команду или remote URL

```bash
git remote add local-gitlab http://localhost:18081/root/java-aqa-cicd-example.git
git push local-gitlab main
```

Git запросит логин и пароль. Логин — `root`, пароль — Personal Access Token

Открой `Build` → `Pipelines`. GitLab прочитает `.gitlab-ci.yml` из репозитория и запустит Validate, API, Integration, UI и Allure jobs

Внутри job доступны console log и JUnit. Готовый Allure HTML скачивается из artifacts job `allure_dev`

## Проверка красной сборки

В учебной ветке временно измени ожидаемый результат одного теста и сделай push. Pipeline должен стать красным. Открой упавший job и проверь, что JUnit XML и `allure-results` сохранились в artifacts

## Остановка

```bash
docker compose --env-file infra/local-ci/.env \
  -f infra/local-ci/docker-compose.gitlab.yml down
```

Конфигурация, репозитории и история сборок остаются в named volumes. Не добавляй `--volumes`, пока эти данные нужны

GitLab должен быть доступен только с учебной машины. Не публикуй порты в интернет без TLS, резервного копирования и отдельной настройки безопасности
