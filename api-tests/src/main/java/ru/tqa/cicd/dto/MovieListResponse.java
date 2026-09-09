package ru.tqa.cicd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MovieListResponse(
    List<MovieResponse> movies,
    int count,
    int page,
    int pageSize,
    int pageCount
) {
}
