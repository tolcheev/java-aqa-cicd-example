package ru.tqa.cicd.integration.db;

import java.util.UUID;

public record TestOrder(UUID id, String productName, int quantity) {
}
