package ru.tqa.cicd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginResponse(UserResponse user, String accessToken, long expiresIn) {
}
