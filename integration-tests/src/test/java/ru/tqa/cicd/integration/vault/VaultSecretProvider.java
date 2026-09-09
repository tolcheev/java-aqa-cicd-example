package ru.tqa.cicd.integration.vault;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class VaultSecretProvider {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient httpClient;
    private final String address;
    private final String token;

    public VaultSecretProvider(String address, String token) {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        this.address = withoutTrailingSlash(address);
        this.token = requireNotBlank(token, "VAULT_TOKEN");
    }

    public void write(String path, Map<String, String> secrets) {
        HttpRequest request = baseRequest(path)
            .POST(HttpRequest.BodyPublishers.ofString(writeBody(secrets)))
            .build();
        send(request, 204, 200);
    }

    public String read(String path, String key) {
        HttpRequest request = baseRequest(path).GET().build();
        String response = send(request, 200);
        try {
            JsonNode value = MAPPER.readTree(response)
                .path("data")
                .path("data")
                .path(key);
            if (!value.isTextual()) {
                throw new IllegalArgumentException("В Vault не найден ключ " + key);
            }
            return value.textValue();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Vault вернул некорректный JSON", exception);
        }
    }

    private HttpRequest.Builder baseRequest(String path) {
        String safePath = requireSafePath(path);
        return HttpRequest.newBuilder(URI.create(address + "/v1/" + safePath))
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/json")
            .header("X-Vault-Token", token);
    }

    private String send(HttpRequest request, int... expectedStatuses) {
        try {
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
            for (int expectedStatus : expectedStatuses) {
                if (response.statusCode() == expectedStatus) {
                    return response.body();
                }
            }
            throw new IllegalStateException("Vault ответил HTTP " + response.statusCode());
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось подключиться к Vault", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Ожидание ответа Vault прервано", exception);
        }
    }

    private String writeBody(Map<String, String> secrets) {
        try {
            return MAPPER.writeValueAsString(Map.of("data", secrets));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Не удалось подготовить запрос к Vault", exception);
        }
    }

    private String requireSafePath(String path) {
        String normalized = requireNotBlank(path, "Vault secret path").replaceFirst("^/+", "");
        if (!normalized.matches("[A-Za-z0-9/_-]+")) {
            throw new IllegalArgumentException("Некорректный Vault secret path");
        }
        return normalized;
    }

    private String requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " не задан");
        }
        return value.trim();
    }

    private String withoutTrailingSlash(String value) {
        String result = requireNotBlank(value, "VAULT_ADDR");
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
