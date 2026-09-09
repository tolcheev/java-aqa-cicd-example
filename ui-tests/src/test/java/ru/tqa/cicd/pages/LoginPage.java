package ru.tqa.cicd.pages;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;
import ru.tqa.cicd.config.EnvironmentConfig;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public final class LoginPage {
    private final EnvironmentConfig environment;

    public LoginPage(EnvironmentConfig environment) {
        this.environment = environment;
    }

    @Step("Открыть страницу входа")
    public LoginPage open() {
        Selenide.open(environment.webUrl() + "/login");
        return this;
    }

    @Step("Войти через UI")
    public LoginPage loginAs(String login, String email, String password) {
        var identity = $("[data-qa-id='login_login_input'], [data-qa-id='login_email_input']")
            .shouldBe(visible);
        identity.setValue("login_login_input".equals(identity.getAttribute("data-qa-id")) ? login : email);
        $("[data-qa-id='login_password_input']").setValue(password);
        $("[data-qa-id='login_submit_button']").click();
        return this;
    }
}
