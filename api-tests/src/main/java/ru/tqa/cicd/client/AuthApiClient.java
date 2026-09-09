package ru.tqa.cicd.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ru.tqa.cicd.config.EnvironmentConfig;
import ru.tqa.cicd.dto.LoginRequest;
import ru.tqa.cicd.dto.LoginResponse;
import ru.tqa.cicd.dto.RegisterUserRequest;
import ru.tqa.cicd.dto.UserResponse;

import static io.restassured.RestAssured.given;

public final class AuthApiClient {
    private final RequestSpecification specification;

    public AuthApiClient(EnvironmentConfig environment) {
        this.specification = ApiSpecifications.auth(environment);
    }

    @Step("Зарегистрировать тестового пользователя через API")
    public UserResponse register(RegisterUserRequest request) {
        return registerResponse(request)
            .then()
            .statusCode(201)
            .extract()
            .as(UserResponse.class);
    }

    public Response registerResponse(RegisterUserRequest request) {
        return given()
            .spec(specification)
            .body(request)
            .when()
            .post("/register");
    }

    @Step("Войти тестовым пользователем через API")
    public LoginResponse login(LoginRequest request) {
        return loginResponse(request)
            .then()
            .statusCode(200)
            .extract()
            .as(LoginResponse.class);
    }

    public Response loginResponse(LoginRequest request) {
        return given()
            .spec(specification)
            .body(request)
            .when()
            .post("/login");
    }

    @Step("Удалить тестового пользователя через API")
    public void deleteUser(String userId, String accessToken) {
        given()
            .spec(specification)
            .header("Authorization", "Bearer " + accessToken)
            .when()
            .delete("/user/{id}", userId)
            .then()
            .statusCode(200);
    }

    public void deleteUserIfPossible(String userId, RegisterUserRequest user) {
        try {
            LoginResponse login = login(new LoginRequest(user.email(), user.password()));
            deleteUser(userId, login.accessToken());
        } catch (RuntimeException ignored) {
            // Cleanup не должен скрывать исходную ошибку теста.
        }
    }
}
