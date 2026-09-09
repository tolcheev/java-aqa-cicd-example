package ru.tqa.cicd.integration.vault;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import ru.tqa.cicd.config.EnvironmentConfig;
import ru.tqa.cicd.integration.IntegrationTestBase;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VaultSecretProviderTest extends IntegrationTestBase {
    private final Faker faker = new Faker();

    @Test
    void writesAndReadsSecretThroughVaultKvV2Api() {
        EnvironmentConfig environment = EnvironmentConfig.fromSystem();
        VaultSecretProvider vault = new VaultSecretProvider(
            connection().vaultAddress(),
            connection().vaultToken()
        );
        String path = environment.vaultSecretPath() + "/example-" + UUID.randomUUID();
        String expected = faker.internet().password(16, 24, true, true, true);

        vault.write(path, Map.of("test-password", expected));

        assertThat(vault.read(path, "test-password")).isEqualTo(expected);
    }
}
