package ru.tqa.cicd.fixture;

import ru.tqa.cicd.client.AuthApiClient;
import ru.tqa.cicd.config.EnvironmentConfig;
import ru.tqa.cicd.data.TestUserFactory;
import ru.tqa.cicd.dto.LoginRequest;
import ru.tqa.cicd.dto.LoginResponse;
import ru.tqa.cicd.dto.RegisterUserRequest;
import ru.tqa.cicd.dto.UserResponse;

public record UserFixture(
    AuthApiClient auth,
    RegisterUserRequest request,
    UserResponse created,
    LoginResponse login
) {
    public static UserFixture create(EnvironmentConfig environment) {
        AuthApiClient auth = new AuthApiClient(environment);
        RegisterUserRequest request = TestUserFactory.uniqueUser();
        UserResponse created = auth.register(request);
        try {
            LoginResponse login = auth.login(new LoginRequest(request.email(), request.password()));
            return new UserFixture(auth, request, created, login);
        } catch (RuntimeException error) {
            auth.deleteUserIfPossible(created.id(), request);
            throw error;
        }
    }

    public void delete() {
        auth.deleteUser(created.id(), login.accessToken());
    }
}
