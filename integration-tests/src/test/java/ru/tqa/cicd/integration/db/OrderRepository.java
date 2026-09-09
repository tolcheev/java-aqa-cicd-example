package ru.tqa.cicd.integration.db;

import ru.tqa.cicd.integration.IntegrationConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

public final class OrderRepository {
    private final IntegrationConnection settings;

    public OrderRepository(IntegrationConnection settings) {
        this.settings = settings;
        createSchema();
    }

    public void save(TestOrder order) {
        String sql = "INSERT INTO test_orders (id, product_name, quantity) VALUES (?, ?, ?)";
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, order.id());
            statement.setString(2, order.productName());
            statement.setInt(3, order.quantity());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw databaseError("сохранить заказ", exception);
        }
    }

    public Optional<TestOrder> findById(UUID id) {
        String sql = "SELECT id, product_name, quantity FROM test_orders WHERE id = ?";
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                return Optional.of(new TestOrder(
                    result.getObject("id", UUID.class),
                    result.getString("product_name"),
                    result.getInt("quantity")
                ));
            }
        } catch (SQLException exception) {
            throw databaseError("прочитать заказ", exception);
        }
    }

    public void delete(UUID id) {
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(
            "DELETE FROM test_orders WHERE id = ?"
        )) {
            statement.setObject(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw databaseError("удалить заказ", exception);
        }
    }

    private void createSchema() {
        String sql = """
            CREATE TABLE IF NOT EXISTS test_orders (
                id UUID PRIMARY KEY,
                product_name VARCHAR(255) NOT NULL,
                quantity INTEGER NOT NULL CHECK (quantity > 0)
            )
            """;
        try (Connection connection = open(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            throw databaseError("создать схему", exception);
        }
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(
            settings.jdbcUrl(),
            settings.databaseUsername(),
            settings.databasePassword()
        );
    }

    private IllegalStateException databaseError(String action, SQLException cause) {
        return new IllegalStateException("Не удалось " + action + " через JDBC", cause);
    }
}
