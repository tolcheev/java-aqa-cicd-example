# Локальная Jenkins-лаба

Jenkins запускается в Docker с Java 21, Docker CLI, Pipeline, JUnit, Allure и готовой задачей `java-aqa-cicd-example`

## Требования

- Docker Engine и Docker Compose v2
- Git
- 6 ГБ свободной памяти

## Запуск

Создай локальный файл настроек

```bash
test -f infra/local-ci/.env || cp infra/local-ci/.env.example infra/local-ci/.env
```

Замени `JENKINS_ADMIN_PASSWORD`. Если работаешь со своим форком, поменяй `JENKINS_REPOSITORY_URL`

```bash
./scripts/start-local-jenkins.sh
```

Открой `http://localhost:18080` и войди под `JENKINS_ADMIN_ID` и `JENKINS_ADMIN_PASSWORD` из локального `.env`

## Первый pipeline

1. Открой `java-aqa-cicd-example`
2. Нажми `Build Now`
3. Открой номер сборки, затем `Console Output`
4. В Stage View проверь Validate, API tests, Integration tests, Start Selenoid и UI tests
5. После завершения открой JUnit и Allure Report

Jenkins читает `Jenkinsfile` из Git. Измени pipeline в репозитории, сделай commit и запусти сборку ещё раз — новая версия файла применится автоматически

## Остановка

```bash
docker compose --env-file infra/local-ci/.env \
  -f infra/local-ci/docker-compose.jenkins.yml down
```

Команда сохраняет Jenkins volume. Для полного сброса можно удалить volume отдельно, но вместе с ним исчезнут настройки и история сборок

Контейнер запускается от root только ради доступа к локальному Docker socket. Такой вариант подходит для учебной машины, но не для рабочего Jenkins

`SELENOID_HOST_OVERRIDE=host.docker.internal` нужен потому, что Jenkins работает внутри контейнера, а Selenoid публикуется на Docker host. На обычном Jenkins agent pipeline оставляет `localhost`
