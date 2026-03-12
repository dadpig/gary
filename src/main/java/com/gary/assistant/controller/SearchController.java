package com.gary.assistant.controller;

import com.gary.assistant.dto.ProductResponse;
import com.gary.assistant.dto.SearchRequest;
import com.gary.assistant.dto.SearchResponse;
import com.gary.assistant.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Product search endpoints")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    private final ProductService productService;

    public SearchController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(
        summary = "Search products across platforms",
        description = "Search for products on Amazon and/or Mercado Livre"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Search completed successfully",
        content = @Content(schema = @Schema(implementation = SearchResponse.class))
    )
    public ResponseEntity<SearchResponse> search(@Valid @RequestBody SearchRequest request) {
        logger.info("Received search request for: {}", request.query());
        var response = productService.search(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get product by ID",
        description = "Retrieve detailed information about a specific product"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Product found",
        content = @Content(schema = @Schema(implementation = ProductResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String id) {
        logger.info("Fetching product with id: {}", id);
        var product = productService.getProduct(id);
        return ResponseEntity.ok(product);
    }
}
