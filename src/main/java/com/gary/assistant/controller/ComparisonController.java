package com.gary.assistant.controller;

import com.gary.assistant.dto.ComparisonRequest;
import com.gary.assistant.dto.ComparisonResponse;
import com.gary.assistant.service.ComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/compare")
@Tag(name = "Comparison", description = "Product comparison endpoints")
public class ComparisonController {

    private static final Logger logger = LoggerFactory.getLogger(ComparisonController.class);

    private final ComparisonService comparisonService;

    public ComparisonController(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @PostMapping
    @Operation(
        summary = "Compare products",
        description = "Compare multiple products side-by-side to find the best deal"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Comparison completed successfully",
        content = @Content(schema = @Schema(implementation = ComparisonResponse.class))
    )
    public ResponseEntity<ComparisonResponse> compare(@Valid @RequestBody ComparisonRequest request) {
        logger.info("Comparing {} products", request.productIds().size());
        var response = comparisonService.compareProducts(request.productIds());
        return ResponseEntity.ok(response);
    }
}
