package ru.tqa.cicd.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;

public abstract class IntegrationTestBase {
    @BeforeAll
    static void requireDockerForTestcontainersMode() {
        boolean external = IntegrationEnvironment.mode(System.getenv()) == IntegrationMode.EXTERNAL;
        Assumptions.assumeTrue(
            external || DockerClientFactory.instance().isDockerAvailable(),
            "Для Testcontainers нужен запущенный Docker Engine"
        );
    }

    protected static IntegrationConnection connection() {
        return IntegrationStack.connection();
    }
}
