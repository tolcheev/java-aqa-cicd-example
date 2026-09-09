package ru.tqa.cicd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReviewResponse(
    String userId,
    int rating,
    String text,
    boolean hidden,
    String createdAt,
    ReviewUser user
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReviewUser(String fullName) {
    }
}
