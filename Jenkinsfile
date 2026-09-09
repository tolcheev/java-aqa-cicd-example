def withTargetEnvironment(Closure body) {
    if (params.TEST_ENV == 'uat') {
        // Эти ID создаются в Jenkins Credentials. Значения в репозиторий не попадают.
        withCredentials([
            string(credentialsId: 'tqa-uat-web-url', variable: 'UAT_WEB'),
            string(credentialsId: 'tqa-uat-auth-api-url', variable: 'UAT_AUTH'),
            string(credentialsId: 'tqa-uat-movies-api-url', variable: 'UAT_MOVIES')
        ]) {
            withEnv([
                'TEST_ENV=uat',
                "UAT_WEB_URL=${UAT_WEB}",
                "UAT_AUTH_API_URL=${UAT_AUTH}",
                "UAT_MOVIES_API_URL=${UAT_MOVIES}"
            ]) {
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
    agent { label 'docker-java-21' }

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
        SELENOID_URL = 'http://localhost:4444/wd/hub'
        SELENOID_STATUS_URL = 'http://localhost:4444/status'
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
            sh 'docker compose -f infra/selenoid/docker-compose.yml down || true'
            junit allowEmptyResults: true, testResults: '**/build/test-results/test/TEST-*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: '**/build/reports/tests/**, **/build/selenide-reports/**, selenoid-logs/**'
            allure includeProperties: false, jdk: '', results: [
                [path: 'api-tests/build/allure-results'],
                [path: 'ui-tests/build/allure-results']
            ]
        }
    }
}
