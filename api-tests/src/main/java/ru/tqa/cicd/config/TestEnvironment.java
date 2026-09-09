package ru.tqa.cicd.config;

import java.util.Locale;

public enum TestEnvironment {
    DEV,
    UAT;

    static TestEnvironment parse(String rawValue) {
        String normalized = rawValue == null || rawValue.isBlank()
            ? "dev"
            : rawValue.trim().toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "dev" -> DEV;
            case "uat" -> UAT;
            case "prod", "production" -> throw new IllegalArgumentException(
                "Окружение prod запрещено для учебных автотестов"
            );
            default -> throw new IllegalArgumentException(
                "Неизвестное окружение: " + normalized + ". Допустимы dev и uat"
            );
        };
    }
}
