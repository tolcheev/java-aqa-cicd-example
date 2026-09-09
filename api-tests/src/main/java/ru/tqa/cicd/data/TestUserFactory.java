package ru.tqa.cicd.data;

import ru.tqa.cicd.dto.RegisterUserRequest;

import java.util.UUID;

public final class TestUserFactory {
    private static final String PASSWORD = "StudentAqa123!";

    private TestUserFactory() {
    }

    public static RegisterUserRequest uniqueUser() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String nameSuffix = suffix.replaceAll("[0-9]", "a");
        return RegisterUserRequest.builder()
            .email("aqa-" + suffix + "@example.com")
            .login("aqa_" + suffix)
            .fullName("CI Student " + nameSuffix)
            .password(PASSWORD)
            .passwordRepeat(PASSWORD)
            .build();
    }
}
