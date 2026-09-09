package ru.tqa.cicd.tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.tqa.cicd.pages.LoginPage;
import ru.tqa.cicd.pages.MainPage;

@Tag("ui")
class LoginUiTest extends UiTestBase {
    private final LoginPage loginPage = new LoginPage(environment);
    private final MainPage mainPage = new MainPage(environment);

    @Test
    void userCreatedByApiCanLogInThroughUi() {
        loginPage.open()
            .loginAs(user.request().login(), user.request().email(), user.request().password());

        mainPage.shouldShowLoggedInUser();
    }
}
