package com.gary.assistant.dto;

import com.gary.assistant.model.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Set;

public record SearchRequest(
    @NotBlank(message = "Search query is required")
    String query,

    @Positive(message = "Max price must be positive")
    BigDecimal maxPrice,

    Set<Platform> platforms,

    SortBy sortBy
) {
    public SearchRequest {
        if (platforms == null || platforms.isEmpty()) {
            platforms = Set.of(Platform.AMAZON, Platform.MERCADO_LIVRE);
        }
        if (sortBy == null) {
            sortBy = SortBy.LOWEST_PRICE;
        }
    }

    public enum SortBy {
        LOWEST_PRICE,
        HIGHEST_RATING,
        MOST_REVIEWS
    }
}
