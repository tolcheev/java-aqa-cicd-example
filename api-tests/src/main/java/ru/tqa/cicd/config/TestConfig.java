package ru.tqa.cicd.config;

import org.aeonbits.owner.Config;

public interface TestConfig extends Config {
    @Key("web.url")
    String webUrl();

    @Key("auth.api.url")
    String authApiUrl();

    @Key("movies.api.url")
    String moviesApiUrl();

    @Key("selenoid.url")
    @DefaultValue("")
    String selenoidUrl();

    @Key("database.jdbc.url")
    String databaseJdbcUrl();

    @Key("database.username")
    String databaseUsername();

    @Key("kafka.bootstrap.servers")
    String kafkaBootstrapServers();

    @Key("vault.address")
    String vaultAddress();

    @Key("vault.secret.path")
    String vaultSecretPath();
}
