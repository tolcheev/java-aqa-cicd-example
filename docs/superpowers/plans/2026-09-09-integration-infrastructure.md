# Integration Infrastructure Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Добавить воспроизводимые учебные примеры OWNER, DataFaker, Awaitility, PostgreSQL, Kafka, Vault и локальных Jenkins/GitLab CI.

**Architecture:** `api-tests` продолжает проверять Cinescope и получает OWNER/DataFaker. Новый `integration-tests` запускает PostgreSQL, Kafka и Vault через Testcontainers либо подключается к тем же сервисам из Docker Compose. Отдельный compose-стек поднимает локальные Jenkins и GitLab Runner, которые читают pipeline-файлы репозитория.

**Tech Stack:** Java 21, Gradle, JUnit 5, OWNER, DataFaker, Awaitility, JDBC, PostgreSQL, Apache Kafka clients, Testcontainers, HashiCorp Vault KV v2, Docker Compose, Jenkins, GitLab CI.

---

### Task 1: OWNER configuration and DataFaker

**Files:**
- Modify: `settings.gradle`
- Modify: `build.gradle`
- Modify: `api-tests/build.gradle`
- Replace: `api-tests/src/main/java/ru/tqa/cicd/config/EnvironmentConfig.java`
- Create: `api-tests/src/main/java/ru/tqa/cicd/config/TestConfig.java`
- Create: `api-tests/src/main/resources/config/dev.properties`
- Create: `api-tests/src/main/resources/config/uat.properties`
- Modify: `api-tests/src/main/java/ru/tqa/cicd/data/TestUserFactory.java`
- Modify: `api-tests/src/test/java/ru/tqa/cicd/config/EnvironmentConfigTest.java`

- [ ] Write failing OWNER tests for Dev defaults, UAT overrides, Production rejection and Vault secret paths.
- [ ] Run `./gradlew :api-tests:test --tests '*EnvironmentConfigTest'`; expect compilation failure because `TestConfig` does not exist.
- [ ] Add OWNER `1.0.12` and DataFaker `2.4.3` dependencies.
- [ ] Define `TestConfig` with `@Config.Sources({"system:properties", "system:env", "classpath:config/${env}.properties"})` and keys for web/API URLs, JDBC, Kafka, Vault address and Vault KV path.
- [ ] Make `EnvironmentConfig.fromSystem()` select `dev` or `uat`, reject `prod`, and expose the OWNER proxy without logging secret values.
- [ ] Put only nonsecret defaults in `dev.properties` and `uat.properties`; use `${...}`-free concrete local defaults for integration services.
- [ ] Replace UUID-only names in `TestUserFactory` with DataFaker-generated login/name plus a UUID suffix.
- [ ] Re-run the focused tests and commit with `feat: configure environments with OWNER and DataFaker`.

### Task 2: Integration module and infrastructure selection

**Files:**
- Modify: `settings.gradle`
- Modify: `build.gradle`
- Create: `integration-tests/build.gradle`
- Create: `integration-tests/src/test/java/ru/tqa/cicd/integration/IntegrationEnvironment.java`
- Create: `integration-tests/src/test/java/ru/tqa/cicd/integration/IntegrationEnvironmentTest.java`

- [ ] Write a failing test proving default mode uses Testcontainers and `USE_EXTERNAL_INFRA=true` uses OWNER endpoints.
- [ ] Run `./gradlew :integration-tests:test --tests '*IntegrationEnvironmentTest'`; expect missing class failure.
- [ ] Add Awaitility `4.3.0`, Testcontainers BOM `1.21.3`, PostgreSQL, Kafka, JUnit Jupiter, Kafka clients and PostgreSQL JDBC dependencies.
- [ ] Implement `IntegrationEnvironment` as a JUnit extension that starts shared PostgreSQL, Kafka and Vault containers only in automatic mode and returns one immutable connection record in both modes.
- [ ] Re-run the focused test and commit with `test: add selectable integration environment`.

### Task 3: JDBC example

**Files:**
- Create: `integration-tests/src/test/resources/db/schema.sql`
- Create: `integration-tests/src/test/java/ru/tqa/cicd/integration/db/OrderRepository.java`
- Create: `integration-tests/src/test/java/ru/tqa/cicd/integration/db/OrderRepositoryTest.java`

- [ ] Write a failing test that creates a Faker order, saves it through JDBC, reads it by ID and deletes it.
- [ ] Run the focused test; expect failure because `OrderRepository` is missing.
- [ ] Implement schema creation and prepared statements in `OrderRepository`; use try-with-resources for every connection, statement and result set.
- [ ] Re-run the focused test and commit with `test: demonstrate PostgreSQL access through JDBC`.

### Task 4: Direct Kafka producer and consumer

**Files:**
- Create: `integration-tests/src/test/java/ru/tqa/cicd/integration/kafka/TestEvent.java`
- Create: `integration-tests/src/test/java/ru/tqa/cicd/integration/kafka/KafkaTestClient.java`
- Create: `integration-tests/src/test/java/ru/tqa/cicd/integration/kafka/KafkaRoundTripTest.java`

- [ ] Write a failing test that creates a unique topic, publishes a JSON event and consumes the same key/value.
- [ ] Run the focused test; expect missing `KafkaTestClient` failure.
- [ ] Implement topic creation with `AdminClient`, publishing with `KafkaProducer` and bounded polling with `KafkaConsumer`.
- [ ] Re-run the focused test and commit with `test: demonstrate direct Kafka round trip`.

### Task 5: Asynchronous service flow with Awaitility

**Files:**
- Create: `integration-tests/src/test/java/ru/tqa/cicd/integration/service/AsyncOrderService.java`
- Create: `integration-tests/src/test/java/ru/tqa/cicd/integration/service/AsyncOrderFlowTest.java`

- [ ] Write a failing test that submits an order once, then uses Awaitility to observe its PostgreSQL row and Kafka event without `Thread.sleep`.
- [ ] Run the focused test; expect missing `AsyncOrderService` failure.
- [ ] Implement the service with `CompletableFuture`, `OrderRepository` and `KafkaTestClient`; return the future so failures are observable.
- [ ] Re-run the focused test and commit with `test: demonstrate asynchronous event verification`.

### Task 6: Real Vault KV v2 connection

**Files:**
- Create: `integration-tests/src/test/java/ru/tqa/cicd/integration/vault/VaultSecretProvider.java`
- Create: `integration-tests/src/test/java/ru/tqa/cicd/integration/vault/VaultSecretProviderTest.java`

- [ ] Write a failing test that writes `data.test-password` to `/v1/secret/data/java-aqa/dev`, reads it through the provider and checks the returned value.
- [ ] Run the focused test; expect missing `VaultSecretProvider` failure.
- [ ] Implement Vault HTTP calls with Java `HttpClient`, `X-Vault-Token` and Jackson. Parse KV v2 as `data.data`; never include the token or secret in exception messages.
- [ ] Read the path from OWNER: `vault.secret.path=secret/data/java-aqa/dev` or `secret/data/java-aqa/uat`.
- [ ] Re-run the focused test and commit with `test: demonstrate Vault KV secret loading`.

### Task 7: Manual PostgreSQL, Kafka and Vault Compose stack

**Files:**
- Create: `infra/integration/docker-compose.yml`
- Create: `infra/integration/.env.example`
- Create: `infra/integration/init-vault.sh`
- Create: `scripts/run-integration-tests.sh`
- Create: `docs/integration-tests.md`

- [ ] Add PostgreSQL `16.4-alpine`, Kafka `7.7.1` in KRaft mode and Vault `1.21.1` with health checks and named volumes.
- [ ] Add a one-shot Vault init service that writes different nonproduction example secrets to Dev and UAT paths.
- [ ] Make `run-integration-tests.sh` start the stack, wait for health, run tests with `USE_EXTERNAL_INFRA=true`, and stop containers without deleting volumes.
- [ ] Document automatic Testcontainers mode, manual Compose mode, connection commands and expected output.
- [ ] Validate with `docker compose -f infra/integration/docker-compose.yml config` and commit with `ci: add local integration services`.

### Task 8: Run integration examples in GitLab and Jenkins

**Files:**
- Modify: `.gitlab-ci.yml`
- Modify: `Jenkinsfile`
- Modify: `docs/gitlab.md`
- Modify: `docs/jenkins.md`

- [ ] Add an `integration_tests` stage/job with Docker access, `./gradlew :integration-tests:test`, JUnit XML and Allure artifacts.
- [ ] Add Jenkins `Integration tests` stage before Selenoid; archive the third module’s reports.
- [ ] Pass `VAULT_ADDR`, `VAULT_TOKEN` and secret path only through CI environment/credentials for external mode; default CI mode uses the isolated Vault container.
- [ ] Parse YAML, run `git diff --check` and commit with `ci: run database Kafka and Vault examples`.

### Task 9: Local Jenkins lab

**Files:**
- Create: `infra/local-ci/jenkins/Dockerfile`
- Create: `infra/local-ci/jenkins/plugins.txt`
- Create: `infra/local-ci/jenkins/casc.yaml`
- Create: `infra/local-ci/jenkins/jobs.groovy`
- Create: `infra/local-ci/docker-compose.jenkins.yml`
- Create: `scripts/start-local-jenkins.sh`
- Create: `docs/local-jenkins.md`

- [ ] Build a Jenkins LTS JDK 21 image with Docker CLI, Pipeline, Git, JUnit, Job DSL, Configuration as Code and Allure plugins.
- [ ] Configure local admin credentials from untracked environment variables and create a Pipeline from SCM job pointing to the repository URL supplied through `JENKINS_REPOSITORY_URL`.
- [ ] Mount Docker socket and a named Jenkins volume; use ports `18080` and `50000` by default.
- [ ] Document startup, login, Build Now, stage/log inspection, Allure, deliberate failure and safe shutdown.
- [ ] Validate compose rendering and commit with `ci: add local Jenkins lab`.

### Task 10: Local GitLab and Runner lab

**Files:**
- Create: `infra/local-ci/docker-compose.gitlab.yml`
- Create: `infra/local-ci/.env.example`
- Create: `scripts/start-local-gitlab.sh`
- Create: `scripts/register-local-gitlab-runner.sh`
- Create: `docs/local-gitlab.md`

- [ ] Configure GitLab CE, Runner and persistent named volumes on ports `18081`, `18443` and `18222`.
- [ ] Add scripts that wait for GitLab health and register a Docker executor with privileged mode for Testcontainers.
- [ ] Document obtaining the initial root password, creating a project, generating a runner token, pushing the repository and opening the pipeline.
- [ ] Include memory guidance: run Jenkins and GitLab separately below 12 GB available RAM.
- [ ] Validate compose rendering and commit with `ci: add local GitLab lab`.

### Task 11: Documentation and final verification

**Files:**
- Modify: `README.md`
- Modify: `docs/adaptation-guide.md`
- Modify: `scripts/verify-no-secrets.sh`
- Modify: `.github/dependabot.yml`

- [ ] Link the new module and all three local guides from README.
- [ ] Explain which values belong in properties, CI variables and Vault.
- [ ] Extend secret scanning for Vault tokens and committed local `.env` files.
- [ ] Run `./gradlew clean check --no-build-cache --rerun-tasks`.
- [ ] Run all Allure report tasks and confirm their `index.html` files exist.
- [ ] Validate every shell script, YAML, JSON and Docker Compose config without starting a browser.
- [ ] Run `git diff --check`, secret scan and placeholder scan.
- [ ] Push `main` and verify the public GitHub branch points to the final commit.
