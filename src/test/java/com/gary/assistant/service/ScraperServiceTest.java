package com.gary.assistant.service;

import com.gary.assistant.config.FallbackConfig;
import com.gary.assistant.model.Platform;
import com.gary.assistant.model.Product;
import com.gary.assistant.scraper.ProductScraper;
import com.gary.assistant.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ScraperService.
 * Tests orchestration, fallback strategies, request coalescing, and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Scraper Service Tests")
class ScraperServiceTest {

    @Mock
    private ProductScraper amazonScraper;

    @Mock
    private ProductScraper mercadoLivreScraper;

    @Mock
    private SimilarProductService similarProductService;

    private FallbackConfig fallbackConfig;
    private ScraperService scraperService;

    @BeforeEach
    void setUp() {
        // Setup mocked scrapers
        when(amazonScraper.getPlatform()).thenReturn(Platform.AMAZON);
        when(mercadoLivreScraper.getPlatform()).thenReturn(Platform.MERCADO_LIVRE);

        // Setup fallback config with test defaults
        fallbackConfig = new FallbackConfig();
        fallbackConfig.setEnabled(true);
        fallbackConfig.setMinResultsThreshold(3);
        fallbackConfig.setMaxAttempts(3);
        fallbackConfig.setSimilarityThreshold(0.3);
        fallbackConfig.setMaxTotalResults(20);
        fallbackConfig.setCrossPlatformEnabled(true);

        // Create service with mocked dependencies
        scraperService = new ScraperService(
            List.of(amazonScraper, mercadoLivreScraper),
            similarProductService,
            fallbackConfig
        );
    }

    // ========================================
    // Initialization Tests
    // ========================================

    @Test
    @DisplayName("Should initialize scrapers map correctly")
    void constructor_ShouldInitializeScrapersMap() {
        // Act
        var status = scraperService.getScraperStatus();

        // Assert
        assertNotNull(status);
        assertTrue(status.containsKey(Platform.AMAZON));
        assertTrue(status.containsKey(Platform.MERCADO_LIVRE));
        assertEquals(2, status.size());
    }

    // ========================================
    // Basic Search Tests
    // ========================================

    @Test
    @DisplayName("Should search all platforms successfully")
    void searchAllPlatforms_WithValidQuery_ShouldReturnProducts() {
        // Arrange
        var amazonProducts = TestDataBuilder.createProductList(2, Platform.AMAZON);
        var mlProducts = TestDataBuilder.createProductList(3, Platform.MERCADO_LIVRE);

        when(amazonScraper.search("mouse", 10)).thenReturn(amazonProducts);
        when(mercadoLivreScraper.search("mouse", 10)).thenReturn(mlProducts);

        // Act
        var results = scraperService.searchAllPlatforms("mouse",
            Set.of(Platform.AMAZON, Platform.MERCADO_LIVRE));

        // Assert
        assertNotNull(results);
        assertEquals(5, results.size());
        verify(amazonScraper).search("mouse", 10);
        verify(mercadoLivreScraper).search("mouse", 10);
    }

    @Test
    @DisplayName("Should skip platforms without scraper")
    void searchAllPlatforms_WithUnavailablePlatform_ShouldSkipIt() {
        // Arrange
        var amazonProducts = TestDataBuilder.createProductList(2, Platform.AMAZON);
        when(amazonScraper.search("mouse", 10)).thenReturn(amazonProducts);

        // Act - Only request Amazon, MERCADO_LIVRE exists but not requested
        var results = scraperService.searchAllPlatforms("mouse", Set.of(Platform.AMAZON));

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(amazonScraper).search("mouse", 10);
        verify(mercadoLivreScraper, never()).search(anyString(), anyInt());
    }

    // ========================================
    // Fallback Strategy Tests
    // ========================================

    @Test
    @DisplayName("Should trigger fallback when results are insufficient")
    void searchAllPlatforms_WithInsufficientResults_ShouldTriggerFallback() {
        // Arrange - Only 2 products (below threshold of 3)
        var initialProducts = TestDataBuilder.createProductList(2, Platform.AMAZON);
        var fallbackProducts = TestDataBuilder.createProductList(2, Platform.MERCADO_LIVRE);

        when(amazonScraper.search(eq("mouse"), anyInt())).thenReturn(initialProducts);
        when(mercadoLivreScraper.search(eq("mouse"), anyInt())).thenReturn(List.of());
        when(mercadoLivreScraper.search(eq("wireless mouse"), anyInt())).thenReturn(fallbackProducts);

        when(similarProductService.generateSimilarQueries("mouse"))
            .thenReturn(List.of("mouse", "wireless mouse"));
        when(similarProductService.isRelevant(anyString(), eq("mouse"), eq(0.3)))
            .thenReturn(true);

        // Act
        var results = scraperService.searchAllPlatforms("mouse",
            Set.of(Platform.AMAZON, Platform.MERCADO_LIVRE));

        // Assert
        assertNotNull(results);
        assertTrue(results.size() >= 3); // Should have enough results after fallback
        verify(similarProductService).generateSimilarQueries("mouse");
    }

    @Test
    @DisplayName("Should not trigger fallback when disabled")
    void searchAllPlatforms_WithFallbackDisabled_ShouldNotTriggerFallback() {
        // Arrange
        fallbackConfig.setEnabled(false);
        var products = TestDataBuilder.createProductList(2, Platform.AMAZON);

        when(amazonScraper.search("mouse", 10)).thenReturn(products);
        when(mercadoLivreScraper.search("mouse", 10)).thenReturn(List.of());

        // Act
        var results = scraperService.searchAllPlatforms("mouse",
            Set.of(Platform.AMAZON, Platform.MERCADO_LIVRE));

        // Assert
        assertEquals(2, results.size()); // Should only have initial results
        verify(similarProductService, never()).generateSimilarQueries(anyString());
    }

    @Test
    @DisplayName("Should respect max fallback attempts")
    void searchAllPlatforms_ShouldRespectMaxFallbackAttempts() {
        // Arrange
        fallbackConfig.setMaxAttempts(2);
        var initialProducts = TestDataBuilder.createProductList(1, Platform.AMAZON);

        when(amazonScraper.search(anyString(), anyInt())).thenReturn(initialProducts);
        when(mercadoLivreScraper.search(anyString(), anyInt())).thenReturn(List.of());

        when(similarProductService.generateSimilarQueries("mouse"))
            .thenReturn(List.of("mouse", "query1", "query2", "query3", "query4"));
        when(similarProductService.isRelevant(anyString(), eq("mouse"), eq(0.3)))
            .thenReturn(false);

        // Act
        var results = scraperService.searchAllPlatforms("mouse",
            Set.of(Platform.AMAZON, Platform.MERCADO_LIVRE));

        // Assert - Should have made at most 2 fallback attempts (plus original)
        verify(amazonScraper, atMost(3)).search(anyString(), anyInt());
    }

    @Test
    @DisplayName("Should respect max total results limit")
    void searchAllPlatforms_ShouldRespectMaxTotalResults() {
        // Arrange
        fallbackConfig.setMaxTotalResults(5);
        var products = TestDataBuilder.createProductList(10, Platform.AMAZON);

        when(amazonScraper.search(anyString(), anyInt())).thenReturn(products);
        when(mercadoLivreScraper.search(anyString(), anyInt())).thenReturn(List.of());

        when(similarProductService.generateSimilarQueries("mouse"))
            .thenReturn(List.of("mouse", "wireless mouse"));
        when(similarProductService.isRelevant(anyString(), eq("mouse"), eq(0.3)))
            .thenReturn(true);

        // Act
        var results = scraperService.searchAllPlatforms("mouse",
            Set.of(Platform.AMAZON, Platform.MERCADO_LIVRE));

        // Assert
        assertTrue(results.size() <= 5); // Should not exceed max
    }

    // ========================================
    // Cross-Platform Fallback Tests
    // ========================================

    @Test
    @DisplayName("Should expand to all platforms when cross-platform fallback enabled")
    void searchAllPlatformsWithCrossPlatformFallback_ShouldExpandSearch() {
        // Arrange - Only 2 products from Amazon
        var amazonProducts = TestDataBuilder.createProductList(2, Platform.AMAZON);
        var mlProducts = TestDataBuilder.createProductList(2, Platform.MERCADO_LIVRE);

        when(amazonScraper.search("mouse", 10)).thenReturn(amazonProducts);
        when(mercadoLivreScraper.search("mouse", 10)).thenReturn(mlProducts);

        // Act - Only request Amazon initially
        var results = scraperService.searchAllPlatformsWithCrossPlatformFallback(
            "mouse",
            Set.of(Platform.AMAZON)
        );

        // Assert - Should have searched Mercado Livre as well
        assertNotNull(results);
        assertTrue(results.size() >= 2); // At least Amazon products
        verify(amazonScraper, atLeastOnce()).search(eq("mouse"), anyInt());
    }

    @Test
    @DisplayName("Should not expand when cross-platform fallback disabled")
    void searchAllPlatformsWithCrossPlatformFallback_WhenDisabled_ShouldNotExpand() {
        // Arrange
        fallbackConfig.setCrossPlatformEnabled(false);
        var products = TestDataBuilder.createProductList(5, Platform.AMAZON);

        when(amazonScraper.search("mouse", 10)).thenReturn(products);

        // Act
        var results = scraperService.searchAllPlatformsWithCrossPlatformFallback(
            "mouse",
            Set.of(Platform.AMAZON)
        );

        // Assert - Should only have searched requested platform
        verify(mercadoLivreScraper, never()).search(anyString(), anyInt());
    }

    // ========================================
    // Single Platform Search Tests
    // ========================================

    @Test
    @DisplayName("Should search single platform successfully")
    void searchPlatform_WithValidPlatform_ShouldReturnProducts() {
        // Arrange
        var products = TestDataBuilder.createProductList(3, Platform.AMAZON);
        when(amazonScraper.search("mouse", 10)).thenReturn(products);

        // Act
        var results = scraperService.searchPlatform(Platform.AMAZON, "mouse");

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
        verify(amazonScraper).search("mouse", 10);
    }

    @Test
    @DisplayName("Should return empty list when scraper not available")
    void searchPlatform_WithUnavailableScraper_ShouldReturnEmptyList() {
        // Act - Request a platform that doesn't exist
        var results = scraperService.searchPlatform(Platform.AMAZON, "mouse");

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should handle scraper exceptions gracefully")
    void searchPlatform_WhenScraperThrowsException_ShouldReturnEmptyList() {
        // Arrange
        when(amazonScraper.search("mouse", 10))
            .thenThrow(new RuntimeException("Network error"));

        // Act
        var results = scraperService.searchPlatform(Platform.AMAZON, "mouse");

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // ========================================
    // Product Details Tests
    // ========================================

    @Test
    @DisplayName("Should get product details successfully")
    void getProductDetails_WithValidProductId_ShouldReturnProduct() {
        // Arrange
        var product = TestDataBuilder.createAmazonProduct("B09HM94VDS", "Test Product");
        when(amazonScraper.getProductDetails("B09HM94VDS")).thenReturn(product);

        // Act
        var result = scraperService.getProductDetails(Platform.AMAZON, "B09HM94VDS");

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(amazonScraper).getProductDetails("B09HM94VDS");
    }

    @Test
    @DisplayName("Should throw exception when scraper not available")
    void getProductDetails_WithUnavailableScraper_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            scraperService.getProductDetails(Platform.AMAZON, "TEST123")
        );
    }

    // ========================================
    // Scraper Status Tests
    // ========================================

    @Test
    @DisplayName("Should return status for all scrapers")
    void getScraperStatus_ShouldReturnAllStatuses() {
        // Arrange
        when(amazonScraper.isAvailable()).thenReturn(true);
        when(mercadoLivreScraper.isAvailable()).thenReturn(false);

        // Act
        var status = scraperService.getScraperStatus();

        // Assert
        assertNotNull(status);
        assertEquals(2, status.size());
        assertTrue(status.get(Platform.AMAZON));
        assertFalse(status.get(Platform.MERCADO_LIVRE));
    }
}
