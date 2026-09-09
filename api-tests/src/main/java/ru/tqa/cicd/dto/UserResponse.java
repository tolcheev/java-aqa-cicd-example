package ru.tqa.cicd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserResponse(
    String id,
    String email,
    String fullName,
    List<String> roles,
    boolean verified,
    String createdAt,
    boolean banned
) {
}
