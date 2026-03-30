package com.gary.assistant.service;

import com.gary.assistant.exception.ProductNotFoundException;
import com.gary.assistant.model.Currency;
import com.gary.assistant.model.Platform;
import com.gary.assistant.model.Price;
import com.gary.assistant.model.Product;
import com.gary.assistant.model.Rating;
import com.gary.assistant.repository.ProductRepository;
import com.gary.assistant.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ComparisonService.
 * Tests product comparison, best deal selection, and value score calculations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Comparison Service Tests")
class ComparisonServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ComparisonService comparisonService;

    @BeforeEach
    void setUp() {
        comparisonService = new ComparisonService(productRepository);
    }

    // ========================================
    // Compare Products Tests
    // ========================================

    @Test
    @DisplayName("Should compare products and identify best deals")
    void compareProducts_WithValidProducts_ShouldReturnComparison() {
        // Arrange
        var product1 = createProductWithPriceAndRating(
            "Product 1",
            new BigDecimal("100"),
            4.5,
            100
        );
        var product2 = createProductWithPriceAndRating(
            "Product 2",
            new BigDecimal("80"), // Cheapest
            4.3,
            50
        );
        var product3 = createProductWithPriceAndRating(
            "Product 3",
            new BigDecimal("120"),
            4.8, // Highest rating
            200  // Most reviews
        );

        var productIds = List.of(
            product1.getId().toString(),
            product2.getId().toString(),
            product3.getId().toString()
        );

        when(productRepository.findById(product1.getId())).thenReturn(Optional.of(product1));
        when(productRepository.findById(product2.getId())).thenReturn(Optional.of(product2));
        when(productRepository.findById(product3.getId())).thenReturn(Optional.of(product3));

        // Act
        var comparison = comparisonService.compareProducts(productIds);

        // Assert
        assertNotNull(comparison);
        assertEquals(3, comparison.products().size());

        // Verify best price (product2 - cheapest)
        assertNotNull(comparison.bestPrice());
        assertEquals(new BigDecimal("80"), comparison.bestPrice().price());
        assertEquals("Product 2", comparison.bestPrice().productName());

        // Verify best value exists
        assertNotNull(comparison.bestValue());

        // Verify metrics
        assertNotNull(comparison.metrics());
        assertFalse(comparison.metrics().isEmpty());

        verify(productRepository, times(3)).findById(any(UUID.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void compareProducts_WithInvalidProductId_ShouldThrowException() {
        // Arrange
        var validProduct = TestDataBuilder.createAmazonProduct();
        var invalidId = UUID.randomUUID();

        when(productRepository.findById(validProduct.getId()))
            .thenReturn(Optional.of(validProduct));
        when(productRepository.findById(invalidId))
            .thenReturn(Optional.empty());

        var productIds = List.of(
            validProduct.getId().toString(),
            invalidId.toString()
        );

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () ->
            comparisonService.compareProducts(productIds)
        );
    }

    // ========================================
    // Best Price Selection Tests
    // ========================================

    @Test
    @DisplayName("Should identify product with lowest total price")
    void compareProducts_ShouldIdentifyLowestPrice() {
        // Arrange
        var cheapProduct = createProductWithPriceAndRating(
            "Cheap",
            new BigDecimal("50"),
            4.0,
            10
        );
        var expensiveProduct = createProductWithPriceAndRating(
            "Expensive",
            new BigDecimal("200"),
            4.5,
            100
        );

        var productIds = List.of(
            cheapProduct.getId().toString(),
            expensiveProduct.getId().toString()
        );

        when(productRepository.findById(cheapProduct.getId()))
            .thenReturn(Optional.of(cheapProduct));
        when(productRepository.findById(expensiveProduct.getId()))
            .thenReturn(Optional.of(expensiveProduct));

        // Act
        var comparison = comparisonService.compareProducts(productIds);

        // Assert
        assertNotNull(comparison.bestPrice());
        assertEquals("Cheap", comparison.bestPrice().productName());
        assertEquals(new BigDecimal("50"), comparison.bestPrice().price());
        assertTrue(comparison.bestPrice().reason().contains("Lowest total cost"));
    }

    // ========================================
    // Best Value Calculation Tests
    // ========================================

    @Test
    @DisplayName("Should calculate best value based on price, rating, and reviews")
    void compareProducts_ShouldCalculateBestValue() {
        // Arrange
        // Product 1: Cheap but low rating
        var product1 = createProductWithPriceAndRating(
            "Cheap Low Rating",
            new BigDecimal("50"),
            3.0,
            10
        );

        // Product 2: Moderate price, excellent rating, many reviews (best value)
        var product2 = createProductWithPriceAndRating(
            "Best Value",
            new BigDecimal("100"),
            4.9,
            5000
        );

        // Product 3: Expensive with good rating
        var product3 = createProductWithPriceAndRating(
            "Expensive Good Rating",
            new BigDecimal("300"),
            4.5,
            100
        );

        var productIds = List.of(
            product1.getId().toString(),
            product2.getId().toString(),
            product3.getId().toString()
        );

        when(productRepository.findById(product1.getId())).thenReturn(Optional.of(product1));
        when(productRepository.findById(product2.getId())).thenReturn(Optional.of(product2));
        when(productRepository.findById(product3.getId())).thenReturn(Optional.of(product3));

        // Act
        var comparison = comparisonService.compareProducts(productIds);

        // Assert
        assertNotNull(comparison.bestValue());
        // Product 2 should have best value due to combination of factors
        assertTrue(comparison.bestValue().reason().contains("combination"));
    }

    // ========================================
    // Comparison Metrics Tests
    // ========================================

    @Test
    @DisplayName("Should generate comprehensive comparison metrics")
    void compareProducts_ShouldGenerateMetrics() {
        // Arrange
        var product1 = createProductWithPriceAndRating(
            "Product 1",
            new BigDecimal("100"), // Cheapest
            4.5,
            100
        );
        var product2 = createProductWithPriceAndRating(
            "Product 2",
            new BigDecimal("200"),
            4.9, // Highest rating
            50
        );
        var product3 = createProductWithPriceAndRating(
            "Product 3",
            new BigDecimal("150"),
            4.3,
            5000 // Most reviews
        );

        var productIds = List.of(
            product1.getId().toString(),
            product2.getId().toString(),
            product3.getId().toString()
        );

        when(productRepository.findById(product1.getId())).thenReturn(Optional.of(product1));
        when(productRepository.findById(product2.getId())).thenReturn(Optional.of(product2));
        when(productRepository.findById(product3.getId())).thenReturn(Optional.of(product3));

        // Act
        var comparison = comparisonService.compareProducts(productIds);

        // Assert
        assertNotNull(comparison.metrics());
        assertEquals(3, comparison.metrics().size());

        // Check that all three metrics are present
        var metricLabels = comparison.metrics().stream()
            .map(m -> m.label())
            .toList();

        assertTrue(metricLabels.contains("Lowest Price"));
        assertTrue(metricLabels.contains("Highest Rating"));
        assertTrue(metricLabels.contains("Most Reviews"));
    }

    // ========================================
    // Helper Methods
    // ========================================

    private Product createProductWithPriceAndRating(
        String name,
        BigDecimal price,
        double rating,
        int reviewCount
    ) {
        var product = new Product(
            name,
            Platform.AMAZON,
            "TEST-" + UUID.randomUUID(),
            "https://example.com/product"
        );

        product.updatePrice(new Price(price, Currency.BRL, BigDecimal.ZERO));
        product.updateRating(new Rating(rating, reviewCount));

        return product;
    }
}
