package ru.tqa.cicd.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.tqa.cicd.config.EnvironmentConfig;
import ru.tqa.cicd.fixture.UserFixture;

abstract class UiTestBase {
    protected final EnvironmentConfig environment = EnvironmentConfig.fromSystem();
    protected UserFixture user;

    @BeforeEach
    void prepareUserAndBrowser() {
        configureBrowser();
        user = UserFixture.create(environment);
    }

    @AfterEach
    void closeBrowserAndDeleteUser() {
        try {
            Selenide.closeWebDriver();
        } finally {
            if (user != null) {
                user.delete();
            }
        }
    }

    private void configureBrowser() {
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10_000;
        Configuration.screenshots = true;
        Configuration.savePageSource = true;
        Configuration.reportsFolder = "build/selenide-reports";

        if (environment.selenoidUrl().isPresent()) {
            // В CI этот адрес указывает на Selenoid; менять нужно только переменную SELENOID_URL.
            Configuration.remote = environment.selenoidUrl().orElseThrow();
            return;
        }

        if (!Boolean.parseBoolean(System.getProperty("remote", "true"))) {
            Configuration.remote = null;
            return;
        }

        throw new IllegalStateException(
            "Для UI-тестов задайте SELENOID_URL или явно включите локальный запуск: -Dremote=false"
        );
    }
}
