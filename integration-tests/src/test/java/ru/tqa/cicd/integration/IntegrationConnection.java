package ru.tqa.cicd.integration;

public record IntegrationConnection(
    String jdbcUrl,
    String databaseUsername,
    String databasePassword,
    String kafkaBootstrapServers,
    String vaultAddress,
    String vaultToken
) {
}
