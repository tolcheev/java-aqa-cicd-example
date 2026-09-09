package ru.tqa.cicd.integration;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationEnvironmentTest {

    @Test
    void usesTestcontainersByDefault() {
        assertThat(IntegrationEnvironment.mode(Map.of()))
            .isEqualTo(IntegrationMode.TESTCONTAINERS);
    }

    @Test
    void usesExternalInfrastructureWhenRequested() {
        assertThat(IntegrationEnvironment.mode(Map.of("USE_EXTERNAL_INFRA", "true")))
            .isEqualTo(IntegrationMode.EXTERNAL);
    }
}
