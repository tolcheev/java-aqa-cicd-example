package ru.tqa.cicd.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record EnvironmentConfig(
    TestEnvironment environment,
    String webUrl,
    String authApiUrl,
    String moviesApiUrl,
    Optional<String> selenoidUrl
) {
    private static final String DEV_WEB_URL = "https://dev-cinescope.t-qa.ru";
    private static final String DEV_AUTH_API_URL = "https://auth.dev-cinescope.t-qa.ru";
    private static final String DEV_MOVIES_API_URL = "https://api.dev-cinescope.t-qa.ru";

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
        Optional<String> selenoidUrl = Optional.ofNullable(
            firstNotBlank(properties.get("selenoidUrl"), environment.get("SELENOID_URL"))
        ).map(EnvironmentConfig::withoutTrailingSlash);

        if (target == TestEnvironment.DEV) {
            return new EnvironmentConfig(
                target,
                DEV_WEB_URL,
                DEV_AUTH_API_URL,
                DEV_MOVIES_API_URL,
                selenoidUrl
            );
        }

        List<String> missing = new ArrayList<>();
        String webUrl = required(environment, "UAT_WEB_URL", missing);
        String authApiUrl = required(environment, "UAT_AUTH_API_URL", missing);
        String moviesApiUrl = required(environment, "UAT_MOVIES_API_URL", missing);
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                "Для uat нужны переменные: " + String.join(", ", missing)
            );
        }

        return new EnvironmentConfig(
            target,
            withoutTrailingSlash(webUrl),
            withoutTrailingSlash(authApiUrl),
            withoutTrailingSlash(moviesApiUrl),
            selenoidUrl
        );
    }

    private static String required(Map<String, String> environment, String name, List<String> missing) {
        String value = environment.get(name);
        if (value == null || value.isBlank()) {
            missing.add(name);
            return "";
        }
        return value.trim();
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
