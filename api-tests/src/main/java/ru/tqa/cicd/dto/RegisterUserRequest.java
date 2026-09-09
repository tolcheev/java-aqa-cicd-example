package ru.tqa.cicd.dto;

import lombok.Builder;

@Builder
public record RegisterUserRequest(
    String email,
    String login,
    String fullName,
    String password,
    String passwordRepeat
) {
}
