package ru.tqa.cicd.integration;

import org.testcontainers.containers.PostgreSQLContainer;
import ru.tqa.cicd.config.EnvironmentConfig;

final class IntegrationStack {
    private static final String LOCAL_DATABASE_PASSWORD = "local-example-only";
    private static final IntegrationConnection CONNECTION = createConnection();

    private IntegrationStack() {
    }

    static IntegrationConnection connection() {
        return CONNECTION;
    }

    private static IntegrationConnection createConnection() {
        EnvironmentConfig config = EnvironmentConfig.fromSystem();
        if (IntegrationEnvironment.mode(System.getenv()) == IntegrationMode.EXTERNAL) {
            return new IntegrationConnection(
                config.testConfig().databaseJdbcUrl(),
                config.testConfig().databaseUsername(),
                requiredEnvironment("DATABASE_PASSWORD"),
                config.testConfig().kafkaBootstrapServers(),
                config.vaultAddress(),
                requiredEnvironment("VAULT_TOKEN")
            );
        }

        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.4-alpine")
            .withDatabaseName("aqa")
            .withUsername("aqa")
            .withPassword(LOCAL_DATABASE_PASSWORD);
        postgres.start();

        return new IntegrationConnection(
            postgres.getJdbcUrl(),
            postgres.getUsername(),
            postgres.getPassword(),
            "",
            "",
            ""
        );
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Для внешней инфраструктуры нужна переменная " + name);
        }
        return value;
    }
}
