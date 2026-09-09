package ru.tqa.cicd.integration.db;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import ru.tqa.cicd.integration.IntegrationTestBase;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRepositoryTest extends IntegrationTestBase {
    private final Faker faker = new Faker();

    @Test
    void savesReadsAndDeletesOrderThroughJdbc() {
        OrderRepository repository = new OrderRepository(connection());
        TestOrder order = new TestOrder(
            UUID.randomUUID(),
            faker.commerce().productName(),
            faker.number().numberBetween(1, 20)
        );

        repository.save(order);

        assertThat(repository.findById(order.id())).contains(order);

        repository.delete(order.id());
        assertThat(repository.findById(order.id())).isEmpty();
    }
}
