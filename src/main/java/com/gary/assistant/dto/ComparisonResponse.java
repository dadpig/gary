package com.gary.assistant.dto;

import java.math.BigDecimal;
import java.util.List;

public record ComparisonResponse(
    List<ProductResponse> products,
    BestDeal bestPrice,
    BestDeal bestValue,
    List<ComparisonMetric> metrics
) {
    public record BestDeal(
        String productId,
        String productName,
        BigDecimal totalCost,
        String reason
    ) {}

    public record ComparisonMetric(
        String name,
        String winner,
        String value,
        String description
    ) {}
}
