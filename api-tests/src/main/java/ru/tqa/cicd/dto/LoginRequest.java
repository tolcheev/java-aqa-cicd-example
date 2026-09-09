package ru.tqa.cicd.dto;

import lombok.Builder;

@Builder
public record LoginRequest(String email, String password) {
}
