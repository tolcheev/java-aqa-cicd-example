package ru.tqa.cicd.pages;

import io.qameta.allure.Step;
import ru.tqa.cicd.config.EnvironmentConfig;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public final class MainPage {
    @Step("Проверить, что пользователь вошёл")
    public MainPage shouldShowLoggedInUser() {
        $("[data-qa-id='profile_page_button']").shouldBe(visible);
        return this;
    }

    @Step("Открыть фильм из списка")
    public MoviePage openMovie(int movieId, EnvironmentConfig environment) {
        $("[data-qa-id='movie_more_" + movieId + "']").click();
        return new MoviePage(environment);
    }
}
