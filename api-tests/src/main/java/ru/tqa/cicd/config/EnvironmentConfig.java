package ru.tqa.cicd.config;

import org.aeonbits.owner.ConfigFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public record EnvironmentConfig(
    TestEnvironment environment,
    String webUrl,
    String authApiUrl,
    String moviesApiUrl,
    Optional<String> selenoidUrl,
    String vaultAddress,
    String vaultSecretPath,
    TestConfig testConfig
) {
    private static final Map<String, String> ENVIRONMENT_KEYS = Map.ofEntries(
        Map.entry("SELENOID_URL", "selenoid.url"),
        Map.entry("UAT_WEB_URL", "web.url"),
        Map.entry("UAT_AUTH_API_URL", "auth.api.url"),
        Map.entry("UAT_MOVIES_API_URL", "movies.api.url"),
        Map.entry("DATABASE_JDBC_URL", "database.jdbc.url"),
        Map.entry("DATABASE_USERNAME", "database.username"),
        Map.entry("KAFKA_BOOTSTRAP_SERVERS", "kafka.bootstrap.servers"),
        Map.entry("VAULT_ADDR", "vault.address"),
        Map.entry("VAULT_SECRET_PATH", "vault.secret.path")
    );

    public static EnvironmentConfig fromSystem() {
        Map<String, String> properties = System.getProperties().entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                entry -> entry.getKey().toString(),
                entry -> entry.getValue().toString()
            ));
        return from(System.getenv(), properties);
    }

    public static EnvironmentConfig from(Map<String, String> environment, Map<String, String> properties) {
        TestEnvironment target = TestEnvironment.parse(
            firstNotBlank(properties.get("env"), environment.get("TEST_ENV"))
        );
        Properties merged = loadDefaults(target);

        ENVIRONMENT_KEYS.forEach((environmentKey, propertyKey) -> {
            String value = environment.get(environmentKey);
            if (value != null && !value.isBlank()) {
                merged.setProperty(propertyKey, value.trim());
            }
        });
        properties.forEach((key, value) -> {
            if (value != null && !value.isBlank()) {
                merged.setProperty(key, value.trim());
            }
        });

        TestConfig config = ConfigFactory.create(TestConfig.class, merged);
        validateRequiredUrls(target, config);
        return new EnvironmentConfig(
            target,
            withoutTrailingSlash(config.webUrl()),
            withoutTrailingSlash(config.authApiUrl()),
            withoutTrailingSlash(config.moviesApiUrl()),
            optionalUrl(config.selenoidUrl()),
            withoutTrailingSlash(config.vaultAddress()),
            config.vaultSecretPath(),
            config
        );
    }

    private static Properties loadDefaults(TestEnvironment environment) {
        String resource = "config/" + environment.name().toLowerCase() + ".properties";
        Properties properties = new Properties();
        try (InputStream input = EnvironmentConfig.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Не найден файл настроек " + resource);
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось прочитать " + resource, exception);
        }
    }

    private static void validateRequiredUrls(TestEnvironment target, TestConfig config) {
        if (target != TestEnvironment.UAT) {
            return;
        }
        List<String> missing = new ArrayList<>();
        require(config.webUrl(), "UAT_WEB_URL", missing);
        require(config.authApiUrl(), "UAT_AUTH_API_URL", missing);
        require(config.moviesApiUrl(), "UAT_MOVIES_API_URL", missing);
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Для uat нужны переменные: " + String.join(", ", missing));
        }
    }

    private static void require(String value, String name, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(name);
        }
    }

    private static Optional<String> optionalUrl(String value) {
        return Optional.ofNullable(firstNotBlank(value, null)).map(EnvironmentConfig::withoutTrailingSlash);
    }

    private static String firstNotBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    private static String withoutTrailingSlash(String value) {
        String result = value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
