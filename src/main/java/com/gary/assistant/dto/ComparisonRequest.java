package com.gary.assistant.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ComparisonRequest(
    @NotEmpty(message = "At least one product ID is required")
    List<String> productIds
) {}
