package ru.tqa.cicd.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ru.tqa.cicd.config.EnvironmentConfig;
import ru.tqa.cicd.dto.MovieListResponse;
import ru.tqa.cicd.dto.MovieResponse;
import ru.tqa.cicd.dto.ReviewRequest;
import ru.tqa.cicd.dto.ReviewResponse;

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

    @Step("Создать отзыв через API")
    public ReviewResponse createReview(int movieId, ReviewRequest request, String accessToken) {
        return given()
            .spec(specification)
            .header("Authorization", "Bearer " + accessToken)
            .body(request)
            .when()
            .post("/movies/{id}/reviews", movieId)
            .then()
            .statusCode(201)
            .extract()
            .as(ReviewResponse.class);
    }

    @Step("Удалить отзыв через API")
    public void deleteReview(int movieId, String accessToken) {
        given()
            .spec(specification)
            .header("Authorization", "Bearer " + accessToken)
            .when()
            .delete("/movies/{id}/reviews", movieId)
            .then()
            .statusCode(200);
    }

    public void deleteReviewIfExists(int movieId, String accessToken) {
        Response response = given()
            .spec(specification)
            .header("Authorization", "Bearer " + accessToken)
            .when()
            .delete("/movies/{id}/reviews", movieId);
        if (response.statusCode() != 200 && response.statusCode() != 404) {
            throw new IllegalStateException(
                "Не удалось удалить тестовый отзыв, HTTP " + response.statusCode()
            );
        }
    }
}
