package ru.tqa.cicd.integration.kafka;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import ru.tqa.cicd.integration.IntegrationTestBase;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaRoundTripTest extends IntegrationTestBase {
    private final Faker faker = new Faker();

    @Test
    void publishesAndConsumesJsonEventDirectly() {
        KafkaTestClient kafka = new KafkaTestClient(connection().kafkaBootstrapServers());
        String topic = "direct-orders-" + UUID.randomUUID();
        String key = UUID.randomUUID().toString();
        TestEvent expected = new TestEvent(
            UUID.randomUUID(),
            faker.commerce().productName(),
            "CREATED"
        );
        kafka.createTopic(topic);

        kafka.publish(topic, key, expected);

        assertThat(kafka.consume(topic, key, Duration.ofSeconds(15))).contains(expected);
    }
}
