package ru.tqa.cicd.tests;

import org.junit.jupiter.api.AfterEach;
import ru.tqa.cicd.client.AuthApiClient;
import ru.tqa.cicd.config.EnvironmentConfig;
import ru.tqa.cicd.data.TestUserFactory;
import ru.tqa.cicd.dto.RegisterUserRequest;
import ru.tqa.cicd.dto.UserResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class ApiTestBase {
    protected final EnvironmentConfig environment = EnvironmentConfig.fromSystem();
    protected final AuthApiClient auth = new AuthApiClient(environment);
    private final List<CreatedUser> createdUsers = new ArrayList<>();

    protected CreatedUser createUser() {
        RegisterUserRequest request = TestUserFactory.uniqueUser();
        UserResponse response = auth.register(request);
        CreatedUser createdUser = new CreatedUser(request, response);
        createdUsers.add(createdUser);
        return createdUser;
    }

    @AfterEach
    void removeCreatedUsers() {
        Collections.reverse(createdUsers);
        createdUsers.forEach(user -> auth.deleteUserIfPossible(user.response().id(), user.request()));
        createdUsers.clear();
    }

    protected record CreatedUser(RegisterUserRequest request, UserResponse response) {
    }
}
