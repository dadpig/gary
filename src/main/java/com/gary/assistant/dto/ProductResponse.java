package com.gary.assistant.dto;

import com.gary.assistant.model.Currency;
import com.gary.assistant.model.Platform;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
    String id,
    String name,
    String description,
    Platform platform,
    String platformProductId,
    String url,
    PriceInfo price,
    RatingInfo rating,
    String imageUrl,
    boolean available,
    Instant lastUpdated
) {
    public record PriceInfo(
        BigDecimal amount,
        Currency currency,
        BigDecimal shippingCost,
        BigDecimal total
    ) {}

    public record RatingInfo(
        Double score,
        Integer reviewCount,
        boolean reliable
    ) {}
}
