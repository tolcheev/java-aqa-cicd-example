package ru.tqa.cicd.integration.kafka;

import java.util.UUID;

public record TestEvent(UUID orderId, String productName, String status) {
}
