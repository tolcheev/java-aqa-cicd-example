def withTargetEnvironment(Closure body) {
    if (params.TEST_ENV == 'uat') {
        // Эти ID создаются в Jenkins Credentials. Значения в репозиторий не попадают.
        withCredentials([
            string(credentialsId: 'tqa-uat-web-url', variable: 'UAT_WEB_URL'),
            string(credentialsId: 'tqa-uat-auth-api-url', variable: 'UAT_AUTH_API_URL'),
            string(credentialsId: 'tqa-uat-movies-api-url', variable: 'UAT_MOVIES_API_URL')
        ]) {
            withEnv(['TEST_ENV=uat']) {
                body()
            }
        }
        return
    }

    withEnv(['TEST_ENV=dev']) {
        body()
    }
}

pipeline {
    agent { label 'tqa-docker-java-21' }

    options {
        timeout(time: 45, unit: 'MINUTES')
        disableConcurrentBuilds()
        timestamps()
        skipDefaultCheckout(true)
    }

    parameters {
        choice(name: 'TEST_ENV', choices: ['dev', 'uat'], description: 'Окружение для тестов')
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        JAVA_TOOL_OPTIONS = '-Dselenide.headless=true'
        // Отдельные порты не пересекаются с Jenkins и Cinescope jobs на общем сервере.
        SELENOID_PORT = '4445'
        SELENOID_UI_PORT = '8091'
        SELENOID_HOST = "${env.SELENOID_HOST_OVERRIDE ?: 'localhost'}"
        SELENOID_URL = "http://${SELENOID_HOST}:4445/wd/hub"
        SELENOID_STATUS_URL = "http://${SELENOID_HOST}:4445/status"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'chmod +x gradlew scripts/*.sh'
            }
        }

        stage('Validate') {
            steps {
                sh 'scripts/verify-no-secrets.sh'
                sh './gradlew :api-tests:compileJava :ui-tests:testClasses'
            }
        }

        stage('API tests') {
            steps {
                script {
                    withTargetEnvironment {
                        sh './gradlew :api-tests:test'
                    }
                }
            }
        }

        stage('Integration tests') {
            environment {
                REQUIRE_DOCKER = 'true'
            }
            steps {
                script {
                    withTargetEnvironment {
                        sh './gradlew :integration-tests:test'
                    }
                }
            }
        }

        stage('Start Selenoid') {
            steps {
                sh 'docker pull selenoid/vnc_chrome:128.0'
                sh 'docker compose -f infra/selenoid/docker-compose.yml up -d'
                sh 'scripts/wait-for-selenoid.sh'
            }
        }

        stage('UI tests') {
            steps {
                script {
                    withTargetEnvironment {
                        sh './gradlew :ui-tests:test'
                    }
                }
            }
        }
    }

    post {
        always {
            sh 'mkdir -p selenoid-logs'
            sh 'docker compose -f infra/selenoid/docker-compose.yml cp selenoid:/opt/selenoid/logs/. selenoid-logs/ || true'
            sh 'docker compose -f infra/selenoid/docker-compose.yml logs > selenoid-logs/services.log || true'
            sh 'docker compose -f infra/selenoid/docker-compose.yml down || true'
            junit allowEmptyResults: true, testResults: '**/build/test-results/test/TEST-*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: '**/build/reports/tests/**, **/build/selenide-reports/**, selenoid-logs/**'
            allure includeProperties: false, jdk: '', results: [
                [path: 'api-tests/build/allure-results'],
                [path: 'integration-tests/build/allure-results'],
                [path: 'ui-tests/build/allure-results']
            ]
        }
    }
}
