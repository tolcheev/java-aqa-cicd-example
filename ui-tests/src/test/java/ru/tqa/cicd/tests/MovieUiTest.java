package ru.tqa.cicd.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.tqa.cicd.client.MoviesApiClient;
import ru.tqa.cicd.dto.MovieResponse;
import ru.tqa.cicd.dto.ReviewRequest;
import ru.tqa.cicd.pages.LoginPage;
import ru.tqa.cicd.pages.MainPage;

import java.util.UUID;

@Tag("ui")
class MovieUiTest extends UiTestBase {
    private final MoviesApiClient movies = new MoviesApiClient(environment);
    private final LoginPage loginPage = new LoginPage(environment);
    private final MainPage mainPage = new MainPage();
    private int movieId;
    private String reviewText;

    @BeforeEach
    void createReviewThroughApi() {
        MovieResponse movie = movies.getMovies(1, 1).movies().getFirst();
        movieId = movie.id();
        reviewText = "UI REST fixture " + UUID.randomUUID();
        movies.createReview(
            movieId,
            new ReviewRequest(reviewText, 5),
            user.login().accessToken()
        );
    }

    @AfterEach
    void removeReviewThroughApi() {
        movies.deleteReviewIfExists(movieId, user.login().accessToken());
    }

    @Test
    void showsReviewCreatedThroughApi() {
        loginPage.open()
            .loginAs(user.request().email(), user.request().password());

        mainPage.openMovie(movieId, environment)
            .shouldShowReview(reviewText);
    }
}
