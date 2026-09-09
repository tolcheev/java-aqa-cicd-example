package ru.tqa.cicd.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ru.tqa.cicd.config.EnvironmentConfig;
import ru.tqa.cicd.dto.MovieListResponse;
import ru.tqa.cicd.dto.MovieResponse;

import static io.restassured.RestAssured.given;

public final class MoviesApiClient {
    private final RequestSpecification specification;

    public MoviesApiClient(EnvironmentConfig environment) {
        this.specification = ApiSpecifications.movies(environment);
    }

    @Step("Получить страницу опубликованных фильмов через API")
    public MovieListResponse getMovies(int page, int pageSize) {
        return given()
            .spec(specification)
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .queryParam("published", true)
            .when()
            .get("/movies")
            .then()
            .statusCode(200)
            .extract()
            .as(MovieListResponse.class);
    }

    @Step("Получить фильм через API")
    public MovieResponse getMovie(int movieId) {
        return getMovieResponse(movieId)
            .then()
            .statusCode(200)
            .extract()
            .as(MovieResponse.class);
    }

    public Response getMovieResponse(int movieId) {
        return given()
            .spec(specification)
            .when()
            .get("/movies/{id}", movieId);
    }
}
