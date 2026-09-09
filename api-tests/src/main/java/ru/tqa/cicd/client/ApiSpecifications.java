package ru.tqa.cicd.client;

import io.restassured.config.LogConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import ru.tqa.cicd.config.EnvironmentConfig;

import static io.restassured.config.RestAssuredConfig.config;
import static io.restassured.filter.log.LogDetail.HEADERS;
import static io.restassured.config.LogConfig.logConfig;

public final class ApiSpecifications {
    private ApiSpecifications() {
    }

    public static RequestSpecification auth(EnvironmentConfig environment) {
        return base(environment.authApiUrl());
    }

    public static RequestSpecification movies(EnvironmentConfig environment) {
        return base(environment.moviesApiUrl());
    }

    private static RequestSpecification base(String baseUri) {
        LogConfig safeLogConfig = logConfig()
            .blacklistHeader("Authorization")
            .blacklistHeader("Cookie")
            .enableLoggingOfRequestAndResponseIfValidationFails(HEADERS);

        return new RequestSpecBuilder()
            .setBaseUri(baseUri)
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .setConfig(config().logConfig(safeLogConfig))
            .build();
    }
}
