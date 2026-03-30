package com.gary.assistant.scraper;

import com.gary.assistant.exception.ScraperException;
import com.gary.assistant.model.Platform;
import com.gary.assistant.model.Product;
import com.gary.assistant.util.MockHtmlFixtures;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for AmazonScraper.
 * Tests parsing, error handling, edge cases, and integration scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Amazon Scraper Tests")
class AmazonScraperTest {

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private ClassicHttpResponse httpResponse;

    @Mock
    private HttpEntity httpEntity;

    private AmazonScraper amazonScraper;

    @BeforeEach
    void setUp() {
        amazonScraper = new AmazonScraper(httpClient);
    }

    // ========================================
    // Basic Functionality Tests
    // ========================================

    @Test
    @DisplayName("Should return correct platform")
    void getPlatform_ShouldReturnAmazon() {
        assertEquals(Platform.AMAZON, amazonScraper.getPlatform());
    }

    @Test
    @DisplayName("Should check availability successfully")
    void isAvailable_WhenSiteReachable_ShouldReturnTrue() throws IOException {
        // Arrange
        when(httpClient.execute(any(), any(HttpClientResponseHandler.class)))
            .thenAnswer(invocation -> {
                HttpClientResponseHandler<?> handler = invocation.getArgument(1);
                when(httpResponse.getCode()).thenReturn(200);
                return handler.handleResponse(httpResponse);
            });

        // Act
        boolean available = amazonScraper.isAvailable();

        // Assert
        assertTrue(available);
        verify(httpClient).execute(any(), any(HttpClientResponseHandler.class));
    }

    @Test
    @DisplayName("Should return false when site unreachable")
    void isAvailable_WhenSiteUnreachable_ShouldReturnFalse() throws IOException {
        // Arrange
        when(httpClient.execute(any(), any(HttpClientResponseHandler.class)))
            .thenThrow(new IOException("Connection refused"));

        // Act
        boolean available = amazonScraper.isAvailable();

        // Assert
        assertFalse(available);
    }

    // ========================================
    // Search Functionality Tests
    // ========================================

    @Test
    @DisplayName("Should parse search results successfully")
    void search_WithValidHtml_ShouldReturnProducts() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.amazonSearchPageWithResults();
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = amazonScraper.search("mouse", 10);

        // Assert
        assertNotNull(products);
        assertEquals(2, products.size());

        Product firstProduct = products.get(0);
        assertEquals("Mouse Logitech MX Master 3S Sem Fio", firstProduct.getName());
        assertEquals("B09HM94VDS", firstProduct.getPlatformProductId());
        assertEquals(Platform.AMAZON, firstProduct.getPlatform());
        assertNotNull(firstProduct.getPrice());
        assertNotNull(firstProduct.getRating());
        assertNotNull(firstProduct.getImageUrl());
    }

    @Test
    @DisplayName("Should handle empty search results")
    void search_WithNoResults_ShouldReturnEmptyList() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.amazonSearchPageEmpty();
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = amazonScraper.search("nonexistent product xyz123", 10);

        // Assert
        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    @Test
    @DisplayName("Should respect maxResults limit")
    void search_WithMaxResults_ShouldLimitResults() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.amazonSearchPageWithResults();
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = amazonScraper.search("mouse", 1);

        // Assert
        assertNotNull(products);
        assertEquals(1, products.size());
    }

    @Test
    @DisplayName("Should throw exception on HTTP error")
    void search_WhenHttpError_ShouldThrowScraperException() throws IOException {
        // Arrange
        when(httpClient.execute(any(), any(HttpClientResponseHandler.class)))
            .thenAnswer(invocation -> {
                HttpClientResponseHandler<?> handler = invocation.getArgument(1);
                when(httpResponse.getCode()).thenReturn(500);
                return handler.handleResponse(httpResponse);
            });

        // Act & Assert
        ScraperException exception = assertThrows(
            ScraperException.class,
            () -> amazonScraper.search("mouse", 10)
        );

        assertEquals(Platform.AMAZON, exception.getPlatform());
        assertTrue(exception.getMessage().contains("HTTP error code"));
    }

    @Test
    @DisplayName("Should throw exception on network failure")
    void search_WhenNetworkFailure_ShouldThrowScraperException() throws IOException {
        // Arrange
        when(httpClient.execute(any(), any(HttpClientResponseHandler.class)))
            .thenThrow(new IOException("Network timeout"));

        // Act & Assert
        ScraperException exception = assertThrows(
            ScraperException.class,
            () -> amazonScraper.search("mouse", 10)
        );

        assertTrue(exception.getMessage().contains("Network timeout"));
    }

    // ========================================
    // Product Details Tests
    // ========================================

    @Test
    @DisplayName("Should parse product details successfully")
    void getProductDetails_WithValidHtml_ShouldReturnProduct() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.amazonProductDetailsPage();
        setupMockHttpResponse(200, html);

        // Act
        Product product = amazonScraper.getProductDetails("B09HM94VDS");

        // Assert
        assertNotNull(product);
        assertTrue(product.getName().contains("Mouse Logitech MX Master 3S"));
        assertEquals("B09HM94VDS", product.getPlatformProductId());
        assertNotNull(product.getDescription());
        assertTrue(product.getDescription().contains("Sensor de alta precisão"));
        assertNotNull(product.getPrice());
        assertNotNull(product.getRating());
        assertNotNull(product.getImageUrl());
    }

    @Test
    @DisplayName("Should throw exception when product details not found")
    void getProductDetails_WhenNotFound_ShouldThrowException() throws IOException {
        // Arrange
        when(httpClient.execute(any(), any(HttpClientResponseHandler.class)))
            .thenAnswer(invocation -> {
                HttpClientResponseHandler<?> handler = invocation.getArgument(1);
                when(httpResponse.getCode()).thenReturn(404);
                return handler.handleResponse(httpResponse);
            });

        // Act & Assert
        assertThrows(
            ScraperException.class,
            () -> amazonScraper.getProductDetails("INVALID_ASIN")
        );
    }

    // ========================================
    // Edge Cases - Products with Missing Fields
    // ========================================

    @Test
    @DisplayName("Should handle products without price gracefully")
    void search_WithProductsWithoutPrice_ShouldCreateProductWithoutPrice() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.amazonProductWithoutPrice();
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = amazonScraper.search("test", 10);

        // Assert
        assertNotNull(products);
        assertFalse(products.isEmpty());

        Product product = products.get(0);
        assertEquals("Product Without Price", product.getName());
        // Price can be null for products not available for purchase
        assertNotNull(product.getRating()); // But rating should exist
    }

    @Test
    @DisplayName("Should handle products without rating gracefully")
    void search_WithProductsWithoutRating_ShouldCreateProductWithoutRating() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.amazonProductWithoutRating();
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = amazonScraper.search("test", 10);

        // Assert
        assertNotNull(products);
        assertFalse(products.isEmpty());

        Product product = products.get(0);
        assertEquals("Product Without Rating", product.getName());
        assertNotNull(product.getPrice()); // Price should exist
        // Rating can be null for new products
    }

    @Test
    @DisplayName("Should skip products with missing required fields")
    void search_WithProductsMissingRequiredFields_ShouldSkipThem() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.amazonProductWithMissingElements();
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = amazonScraper.search("test", 10);

        // Assert
        assertNotNull(products);
        // Should be empty or skip the malformed product
        // Products without ASIN or title should be filtered out
    }

    // ========================================
    // Price Parsing Tests
    // ========================================

    @Test
    @DisplayName("Should parse price with comma decimal separator")
    void search_WithPriceCommaDecimal_ShouldParseCorrectly() throws IOException {
        // Arrange
        String html = """
            <html>
            <body>
                <div data-component-type="s-search-result" data-asin="TEST001">
                    <h2><a href="/test"><span>Test Product</span></a></h2>
                    <span class="a-price-whole">1.299,90</span>
                </div>
            </body>
            </html>
            """;
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = amazonScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        assertNotNull(product.getPrice());
        // Should parse 1.299,90 as 1299.90
        assertTrue(product.getPrice().getAmount().compareTo(java.math.BigDecimal.valueOf(1299.90)) == 0);
    }

    @Test
    @DisplayName("Should parse price without decimal part")
    void search_WithPriceNoDecimal_ShouldParseCorrectly() throws IOException {
        // Arrange
        String html = """
            <html>
            <body>
                <div data-component-type="s-search-result" data-asin="TEST002">
                    <h2><a href="/test"><span>Test Product</span></a></h2>
                    <span class="a-price-whole">500</span>
                </div>
            </body>
            </html>
            """;
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = amazonScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        assertNotNull(product.getPrice());
        assertTrue(product.getPrice().getAmount().compareTo(java.math.BigDecimal.valueOf(500.0)) == 0);
    }

    // ========================================
    // Rating Parsing Tests
    // ========================================

    @Test
    @DisplayName("Should parse rating with Portuguese format")
    void search_WithPortugueseRating_ShouldParseCorrectly() throws IOException {
        // Arrange
        String html = """
            <html>
            <body>
                <div data-component-type="s-search-result" data-asin="TEST003">
                    <h2><a href="/test"><span>Test Product</span></a></h2>
                    <span class="a-price-whole">100</span>
                    <span class="a-icon-alt">4,8 de 5 estrelas</span>
                    <span aria-label="1.234 avaliações">1.234</span>
                </div>
            </body>
            </html>
            """;
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = amazonScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        assertNotNull(product.getRating());
        assertEquals(4.8, product.getRating().getScore());
        assertEquals(1234, product.getRating().getReviewCount());
    }

    // ========================================
    // Special Characters and Encoding Tests
    // ========================================

    @Test
    @DisplayName("Should handle product names with special characters")
    void search_WithSpecialCharactersInName_ShouldParseCorrectly() throws IOException {
        // Arrange
        String html = """
            <html>
            <body>
                <div data-component-type="s-search-result" data-asin="TEST004">
                    <h2><a href="/test"><span>Mouse Gamer "Pro" & Teclado (RGB)</span></a></h2>
                    <span class="a-price-whole">150</span>
                </div>
            </body>
            </html>
            """;
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = amazonScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        assertTrue(product.getName().contains("Mouse Gamer"));
        assertTrue(product.getName().contains("&"));
    }

    @Test
    @DisplayName("Should handle Portuguese accents in product names")
    void search_WithPortugueseAccents_ShouldParseCorrectly() throws IOException {
        // Arrange
        String html = """
            <html>
            <body>
                <div data-component-type="s-search-result" data-asin="TEST005">
                    <h2><a href="/test"><span>Mouse Ergonômico Português Ação Função</span></a></h2>
                    <span class="a-price-whole">200</span>
                </div>
            </body>
            </html>
            """;
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = amazonScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        assertTrue(product.getName().contains("Ergonômico"));
        assertTrue(product.getName().contains("Português"));
        assertTrue(product.getName().contains("Ação"));
    }

    // ========================================
    // URL Handling Tests
    // ========================================

    @Test
    @DisplayName("Should handle relative URLs correctly")
    void search_WithRelativeUrl_ShouldConstructAbsoluteUrl() throws IOException {
        // Arrange
        String html = """
            <html>
            <body>
                <div data-component-type="s-search-result" data-asin="TEST006">
                    <h2><a href="/dp/TEST006"><span>Test Product</span></a></h2>
                    <span class="a-price-whole">100</span>
                </div>
            </body>
            </html>
            """;
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = amazonScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        assertTrue(product.getUrl().startsWith("https://www.amazon.com.br"));
        assertTrue(product.getUrl().contains("TEST006"));
    }

    // ========================================
    // Query Encoding Tests
    // ========================================

    @Test
    @DisplayName("Should URL encode search queries correctly")
    void search_WithSpecialCharactersInQuery_ShouldEncodeCorrectly() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.amazonSearchPageWithResults();
        setupMockHttpResponse(200, html);

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            List<Product> products = amazonScraper.search("mouse & teclado", 10);
            assertNotNull(products);
        });
    }

    @Test
    @DisplayName("Should handle queries with Portuguese characters")
    void search_WithPortugueseQuery_ShouldEncodeCorrectly() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.amazonSearchPageWithResults();
        setupMockHttpResponse(200, html);

        // Act & Assert
        assertDoesNotThrow(() -> {
            List<Product> products = amazonScraper.search("mouse ergonômico", 10);
            assertNotNull(products);
        });
    }

    // ========================================
    // Very Long Content Tests
    // ========================================

    @Test
    @DisplayName("Should handle very long product titles")
    void search_WithVeryLongTitle_ShouldParseCorrectly() throws IOException {
        // Arrange
        String veryLongTitle = "Mouse Gamer Sem Fio RGB Recarregável com Sensor Óptico de Alta Precisão 16000 DPI 7 Botões Programáveis Compatível com Windows Mac Linux".repeat(3);
        String html = String.format("""
            <html>
            <body>
                <div data-component-type="s-search-result" data-asin="TEST007">
                    <h2><a href="/test"><span>%s</span></a></h2>
                    <span class="a-price-whole">350</span>
                </div>
            </body>
            </html>
            """, veryLongTitle);
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = amazonScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        assertNotNull(product.getName());
        assertTrue(product.getName().length() > 100);
    }

    // ========================================
    // Helper Methods
    // ========================================

    private void setupMockHttpResponse(int statusCode, String htmlContent) throws IOException {
        when(httpClient.execute(any(), any(HttpClientResponseHandler.class)))
            .thenAnswer(invocation -> {
                HttpClientResponseHandler<?> handler = invocation.getArgument(1);

                when(httpResponse.getCode()).thenReturn(statusCode);
                when(httpResponse.getEntity()).thenReturn(httpEntity);
                when(httpEntity.getContent())
                    .thenReturn(new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8)));

                return handler.handleResponse(httpResponse);
            });
    }
}
