package ru.tqa.cicd.tests;

import org.junit.jupiter.api.Test;
import ru.tqa.cicd.client.MoviesApiClient;
import ru.tqa.cicd.dto.MovieListResponse;
import ru.tqa.cicd.dto.MovieResponse;

import static org.assertj.core.api.Assertions.assertThat;

class MoviesApiTest extends ApiTestBase {
    private final MoviesApiClient movies = new MoviesApiClient(environment);

    @Test
    void returnsPublishedMovies() {
        MovieListResponse response = movies.getMovies(1, 5);

        assertThat(response.movies()).isNotEmpty();
        assertThat(response.movies()).allMatch(MovieResponse::published);
        assertThat(response.count()).isPositive();
    }

    @Test
    void returnsMovieById() {
        MovieResponse listedMovie = movies.getMovies(1, 1).movies().getFirst();

        MovieResponse movie = movies.getMovie(listedMovie.id());

        assertThat(movie.id()).isEqualTo(listedMovie.id());
        assertThat(movie.name()).isNotBlank();
    }

    @Test
    void returnsNotFoundForUnknownMovie() {
        int statusCode = movies.getMovieResponse(2_147_483_647).statusCode();

        assertThat(statusCode).isEqualTo(404);
    }
}
