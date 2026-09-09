package ru.tqa.cicd.dto;

import lombok.Builder;

@Builder
public record ReviewRequest(String text, int rating) {
}
