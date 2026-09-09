package ru.tqa.cicd.data;

import net.datafaker.Faker;
import ru.tqa.cicd.dto.RegisterUserRequest;

import java.util.Locale;
import java.util.UUID;

public final class TestUserFactory {
    private static final String PASSWORD = "StudentAqa123!";
    private static final Faker FAKER = new Faker(Locale.ENGLISH);

    private TestUserFactory() {
    }

    public static RegisterUserRequest uniqueUser() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return RegisterUserRequest.builder()
            .email("aqa-" + suffix + "@example.com")
            .login("aqa_" + suffix)
            .fullName(FAKER.name().fullName())
            .password(PASSWORD)
            .passwordRepeat(PASSWORD)
            .build();
    }
}
