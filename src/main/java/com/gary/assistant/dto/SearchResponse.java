package com.gary.assistant.dto;

import java.time.Instant;
import java.util.List;

public record SearchResponse(
    String searchId,
    String query,
    List<ProductResponse> products,
    int totalResults,
    Instant searchedAt,
    long durationMs
) {}
