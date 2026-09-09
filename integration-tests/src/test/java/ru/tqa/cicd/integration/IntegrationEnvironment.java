package ru.tqa.cicd.integration;

import java.util.Map;

final class IntegrationEnvironment {
    private IntegrationEnvironment() {
    }

    static IntegrationMode mode(Map<String, String> environment) {
        return Boolean.parseBoolean(environment.getOrDefault("USE_EXTERNAL_INFRA", "false"))
            ? IntegrationMode.EXTERNAL
            : IntegrationMode.TESTCONTAINERS;
    }
}
