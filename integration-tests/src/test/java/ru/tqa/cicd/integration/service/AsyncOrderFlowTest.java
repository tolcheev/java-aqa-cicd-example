package ru.tqa.cicd.integration.service;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import ru.tqa.cicd.integration.IntegrationTestBase;
import ru.tqa.cicd.integration.db.OrderRepository;
import ru.tqa.cicd.integration.db.TestOrder;
import ru.tqa.cicd.integration.kafka.KafkaTestClient;
import ru.tqa.cicd.integration.kafka.TestEvent;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class AsyncOrderFlowTest extends IntegrationTestBase {
    private final Faker faker = new Faker();

    @Test
    void waitsForDatabaseRowAndKafkaEventWithoutThreadSleep() {
        OrderRepository repository = new OrderRepository(connection());
        KafkaTestClient kafka = new KafkaTestClient(connection().kafkaBootstrapServers());
        String topic = "async-orders-" + UUID.randomUUID();
        kafka.createTopic(topic);
        TestOrder order = new TestOrder(
            UUID.randomUUID(),
            faker.commerce().productName(),
            faker.number().numberBetween(1, 20)
        );
        TestEvent expectedEvent = new TestEvent(order.id(), order.productName(), "CREATED");
        AsyncOrderService service = new AsyncOrderService(repository, kafka, topic);

        service.submit(order);

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(repository.findById(order.id())).contains(order);
            assertThat(kafka.consume(topic, order.id().toString(), Duration.ofSeconds(1)))
                .contains(expectedEvent);
        });

        repository.delete(order.id());
    }
}
