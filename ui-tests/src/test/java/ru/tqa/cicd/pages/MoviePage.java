package ru.tqa.cicd.pages;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;
import ru.tqa.cicd.config.EnvironmentConfig;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;

public final class MoviePage {
    private final EnvironmentConfig environment;

    public MoviePage(EnvironmentConfig environment) {
        this.environment = environment;
    }

    @Step("Открыть карточку фильма")
    public MoviePage open(int movieId) {
        Selenide.open(environment.webUrl() + "/movies/" + movieId);
        return this;
    }

    @Step("Проверить отзыв, созданный через API")
    public MoviePage shouldShowReview(String reviewText) {
        $$("p").findBy(exactText(reviewText)).shouldBe(visible);
        return this;
    }
}
