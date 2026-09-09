package ru.tqa.cicd.integration;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;
import ru.tqa.cicd.config.EnvironmentConfig;
import ru.tqa.cicd.integration.vault.VaultSecretProvider;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

final class IntegrationStack {
    private static final IntegrationConnection CONNECTION = createConnection();

    private IntegrationStack() {
    }

    static IntegrationConnection connection() {
        return CONNECTION;
    }

    private static IntegrationConnection createConnection() {
        EnvironmentConfig config = EnvironmentConfig.fromSystem();
        if (IntegrationEnvironment.mode(System.getenv()) == IntegrationMode.EXTERNAL) {
            String vaultToken = requiredEnvironment("VAULT_TOKEN");
            VaultSecretProvider vault = new VaultSecretProvider(config.vaultAddress(), vaultToken);
            return new IntegrationConnection(
                config.testConfig().databaseJdbcUrl(),
                config.testConfig().databaseUsername(),
                vault.read(config.vaultSecretPath(), "database-password"),
                config.testConfig().kafkaBootstrapServers(),
                config.vaultAddress(),
                vaultToken
            );
        }

        String databasePassword = UUID.randomUUID().toString();
        String vaultToken = UUID.randomUUID().toString();
        GenericContainer<?> vaultContainer = new GenericContainer<>(
            DockerImageName.parse("hashicorp/vault:1.21.1")
        )
            .withExposedPorts(8200)
            .withEnv("VAULT_DEV_ROOT_TOKEN_ID", vaultToken)
            .withEnv("VAULT_DEV_LISTEN_ADDRESS", "0.0.0.0:8200")
            .waitingFor(Wait.forHttp("/v1/sys/health")
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofMinutes(2)));
        vaultContainer.start();

        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.4-alpine")
            .withDatabaseName("aqa")
            .withUsername("aqa")
            .withPassword(databasePassword);
        postgres.start();
        ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.7.1")
        );
        kafka.start();

        String vaultAddress = "http://" + vaultContainer.getHost() + ":" + vaultContainer.getMappedPort(8200);
        VaultSecretProvider vault = new VaultSecretProvider(vaultAddress, vaultToken);
        vault.write(config.vaultSecretPath(), Map.of("database-password", databasePassword));

        return new IntegrationConnection(
            postgres.getJdbcUrl(),
            postgres.getUsername(),
            vault.read(config.vaultSecretPath(), "database-password"),
            kafka.getBootstrapServers(),
            vaultAddress,
            vaultToken
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
