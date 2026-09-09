package ru.tqa.cicd.integration.service;

import ru.tqa.cicd.integration.db.OrderRepository;
import ru.tqa.cicd.integration.db.TestOrder;
import ru.tqa.cicd.integration.kafka.KafkaTestClient;
import ru.tqa.cicd.integration.kafka.TestEvent;

import java.util.concurrent.CompletableFuture;

public final class AsyncOrderService {
    private final OrderRepository repository;
    private final KafkaTestClient kafka;
    private final String topic;

    public AsyncOrderService(OrderRepository repository, KafkaTestClient kafka, String topic) {
        this.repository = repository;
        this.kafka = kafka;
        this.topic = topic;
    }

    public CompletableFuture<Void> submit(TestOrder order) {
        return CompletableFuture.runAsync(() -> {
            repository.save(order);
            kafka.publish(
                topic,
                order.id().toString(),
                new TestEvent(order.id(), order.productName(), "CREATED")
            );
        });
    }
}
