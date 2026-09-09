package ru.tqa.cicd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MovieResponse(
    int id,
    String name,
    int price,
    String description,
    String imageUrl,
    String location,
    boolean published,
    int genreId,
    Genre genre,
    String createdAt,
    double rating
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Genre(String name) {
    }
}
