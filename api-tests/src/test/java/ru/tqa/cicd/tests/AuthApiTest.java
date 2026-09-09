package ru.tqa.cicd.tests;

import org.junit.jupiter.api.Test;
import ru.tqa.cicd.dto.LoginRequest;

import static org.assertj.core.api.Assertions.assertThat;

class AuthApiTest extends ApiTestBase {

    @Test
    void registersUniqueUser() {
        CreatedUser user = createUser();

        assertThat(user.response().email()).isEqualTo(user.request().email());
        assertThat(user.response().verified()).isTrue();
    }

    @Test
    void rejectsDuplicateEmail() {
        CreatedUser user = createUser();

        int statusCode = auth.registerResponse(user.request()).statusCode();

        assertThat(statusCode).isEqualTo(409);
    }

    @Test
    void rejectsWrongPassword() {
        CreatedUser user = createUser();

        int statusCode = auth.loginResponse(
            new LoginRequest(user.request().email(), "WrongPassword123!")
        ).statusCode();

        assertThat(statusCode).isEqualTo(401);
    }
}
