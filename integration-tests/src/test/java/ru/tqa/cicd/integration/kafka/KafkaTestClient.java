package ru.tqa.cicd.integration.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public final class KafkaTestClient {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private final String bootstrapServers;

    public KafkaTestClient(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public void createTopic(String topic) {
        try (AdminClient admin = AdminClient.create(Map.of(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers
        ))) {
            admin.createTopics(List.of(new NewTopic(topic, 1, (short) 1)))
                .all()
                .get(15, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось создать Kafka topic " + topic, exception);
        }
    }

    public void publish(String topic, String key, TestEvent event) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties)) {
            producer.send(new ProducerRecord<>(topic, key, toJson(event))).get(15, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось отправить событие в Kafka", exception);
        }
    }

    public Optional<TestEvent> consume(String topic, String expectedKey, Duration timeout) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            TopicPartition partition = new TopicPartition(topic, 0);
            consumer.assign(List.of(partition));
            consumer.seekToBeginning(List.of(partition));
            Instant deadline = Instant.now().plus(timeout);
            while (Instant.now().isBefore(deadline)) {
                for (var record : consumer.poll(Duration.ofMillis(250))) {
                    if (expectedKey.equals(record.key())) {
                        return Optional.of(fromJson(record.value()));
                    }
                }
            }
            return Optional.empty();
        }
    }

    private String toJson(TestEvent event) {
        try {
            return MAPPER.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Не удалось сериализовать Kafka event", exception);
        }
    }

    private TestEvent fromJson(String value) {
        try {
            return MAPPER.readValue(value, TestEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Не удалось прочитать Kafka event", exception);
        }
    }
}
