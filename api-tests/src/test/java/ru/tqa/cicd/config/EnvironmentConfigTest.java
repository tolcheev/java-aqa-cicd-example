package ru.tqa.cicd.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvironmentConfigTest {

    @Test
    void usesCinescopeDevByDefault() {
        EnvironmentConfig config = EnvironmentConfig.from(Map.of(), Map.of());

        assertThat(config.environment()).isEqualTo(TestEnvironment.DEV);
        assertThat(config.webUrl()).isEqualTo("https://dev-cinescope.t-qa.ru");
        assertThat(config.authApiUrl()).isEqualTo("https://auth.dev-cinescope.t-qa.ru");
        assertThat(config.moviesApiUrl()).isEqualTo("https://api.dev-cinescope.t-qa.ru");
        assertThat(config.vaultSecretPath()).isEqualTo("secret/data/java-aqa/dev");
        assertThat(config.testConfig()).isInstanceOf(TestConfig.class);
    }

    @Test
    void requiresEveryUatUrl() {
        assertThatThrownBy(() -> EnvironmentConfig.from(
            Map.of("TEST_ENV", "uat"),
            Map.of()
        )).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("UAT_AUTH_API_URL");
    }

    @Test
    void systemPropertyOverridesEnvironmentVariable() {
        EnvironmentConfig config = EnvironmentConfig.from(
            Map.of(
                "TEST_ENV", "dev",
                "UAT_WEB_URL", "https://uat.example.test",
                "UAT_AUTH_API_URL", "https://auth.uat.example.test",
                "UAT_MOVIES_API_URL", "https://api.uat.example.test"
            ),
            Map.of(
                "env", "uat",
                "web.url", "https://property.uat.example.test"
            )
        );

        assertThat(config.environment()).isEqualTo(TestEnvironment.UAT);
        assertThat(config.webUrl()).isEqualTo("https://property.uat.example.test");
        assertThat(config.vaultSecretPath()).isEqualTo("secret/data/java-aqa/uat");
    }

    @Test
    void rejectsProductionEnvironment() {
        assertThatThrownBy(() -> EnvironmentConfig.from(
            Map.of("TEST_ENV", "prod"),
            Map.of()
        )).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("prod")
            .hasMessageContaining("запрещено");
    }

    @Test
    void readsOptionalSelenoidUrl() {
        EnvironmentConfig config = EnvironmentConfig.from(
            Map.of("SELENOID_URL", "http://selenoid:4444/wd/hub"),
            Map.of()
        );

        assertThat(config.selenoidUrl()).contains("http://selenoid:4444/wd/hub");
    }
}
