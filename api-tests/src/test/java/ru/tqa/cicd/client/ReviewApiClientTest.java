package ru.tqa.cicd.client;

import org.junit.jupiter.api.Test;
import ru.tqa.cicd.config.EnvironmentConfig;
import ru.tqa.cicd.data.TestUserFactory;
import ru.tqa.cicd.dto.LoginRequest;
import ru.tqa.cicd.dto.LoginResponse;
import ru.tqa.cicd.dto.MovieResponse;
import ru.tqa.cicd.dto.RegisterUserRequest;
import ru.tqa.cicd.dto.ReviewRequest;
import ru.tqa.cicd.dto.ReviewResponse;
import ru.tqa.cicd.dto.UserResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewApiClientTest {
    private final EnvironmentConfig environment = EnvironmentConfig.fromSystem();
    private final AuthApiClient auth = new AuthApiClient(environment);
    private final MoviesApiClient movies = new MoviesApiClient(environment);

    @Test
    void createsAndDeletesReviewForApiFixture() {
        RegisterUserRequest user = TestUserFactory.uniqueUser();
        UserResponse createdUser = auth.register(user);
        LoginResponse login = null;
        int movieId = 0;

        try {
            login = auth.login(new LoginRequest(user.email(), user.password()));
            MovieResponse movie = movies.getMovies(1, 1).movies().getFirst();
            movieId = movie.id();
            ReviewRequest review = new ReviewRequest("Отзыв создан через REST fixture", 5);
            ReviewResponse createdReview = movies.createReview(
                movieId, review, login.accessToken()
            );

            assertThat(createdReview.userId()).isEqualTo(createdUser.id());
            assertThat(createdReview.text()).isEqualTo(review.text());

            movies.deleteReview(movieId, login.accessToken());
        } finally {
            if (login != null && movieId > 0) {
                movies.deleteReviewIfExists(movieId, login.accessToken());
            }
            auth.deleteUserIfPossible(createdUser.id(), user);
        }
    }
}
