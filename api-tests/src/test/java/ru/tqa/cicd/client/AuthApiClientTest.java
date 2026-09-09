package ru.tqa.cicd.client;

import org.junit.jupiter.api.Test;
import ru.tqa.cicd.config.EnvironmentConfig;
import ru.tqa.cicd.data.TestUserFactory;
import ru.tqa.cicd.dto.LoginRequest;
import ru.tqa.cicd.dto.LoginResponse;
import ru.tqa.cicd.dto.RegisterUserRequest;
import ru.tqa.cicd.dto.UserResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AuthApiClientTest {

    private final AuthApiClient auth = new AuthApiClient(EnvironmentConfig.fromSystem());

    @Test
    void createsLogsInAndDeletesUser() {
        RegisterUserRequest user = TestUserFactory.uniqueUser();
        UserResponse created = auth.register(user);

        try {
            LoginResponse login = auth.login(new LoginRequest(user.email(), user.password()));

            assertThat(login.user().id()).isEqualTo(created.id());
            assertThat(login.accessToken()).isNotBlank();
            auth.deleteUser(created.id(), login.accessToken());
        } catch (RuntimeException | AssertionError error) {
            auth.deleteUserIfPossible(created.id(), user);
            throw error;
        }
    }
}
