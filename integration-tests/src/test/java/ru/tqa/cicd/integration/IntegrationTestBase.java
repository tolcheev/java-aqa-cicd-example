package ru.tqa.cicd.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;

public abstract class IntegrationTestBase {
    @BeforeAll
    static void requireDockerForTestcontainersMode() {
        boolean external = IntegrationEnvironment.mode(System.getenv()) == IntegrationMode.EXTERNAL;
        boolean dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        if (!external && !dockerAvailable && Boolean.parseBoolean(System.getenv("REQUIRE_DOCKER"))) {
            throw new IllegalStateException("Docker обязателен для интеграционных тестов в CI");
        }
        Assumptions.assumeTrue(
            external || dockerAvailable,
            "Для Testcontainers нужен запущенный Docker Engine"
        );
    }

    protected static IntegrationConnection connection() {
        return IntegrationStack.connection();
    }
}
