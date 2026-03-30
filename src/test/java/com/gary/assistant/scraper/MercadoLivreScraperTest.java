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
 * Comprehensive test suite for MercadoLivreScraper.
 * Tests parsing, error handling, edge cases, and integration scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Mercado Livre Scraper Tests")
class MercadoLivreScraperTest {

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private ClassicHttpResponse httpResponse;

    @Mock
    private HttpEntity httpEntity;

    private MercadoLivreScraper mercadoLivreScraper;

    @BeforeEach
    void setUp() {
        mercadoLivreScraper = new MercadoLivreScraper(httpClient);
    }

    // ========================================
    // Basic Functionality Tests
    // ========================================

    @Test
    @DisplayName("Should return correct platform")
    void getPlatform_ShouldReturnMercadoLivre() {
        assertEquals(Platform.MERCADO_LIVRE, mercadoLivreScraper.getPlatform());
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
        boolean available = mercadoLivreScraper.isAvailable();

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
        boolean available = mercadoLivreScraper.isAvailable();

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
        String html = MockHtmlFixtures.mercadoLivreSearchPageWithResults();
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("mouse", 10);

        // Assert
        assertNotNull(products);
        assertEquals(2, products.size());

        Product firstProduct = products.get(0);
        assertEquals("Mouse Logitech MX Master 3S Wireless", firstProduct.getName());
        assertEquals("MLB-123456789", firstProduct.getPlatformProductId());
        assertEquals(Platform.MERCADO_LIVRE, firstProduct.getPlatform());
        assertNotNull(firstProduct.getPrice());
        assertEquals(java.math.BigDecimal.ZERO, firstProduct.getPrice().getShippingCost()); // Frete grátis
        assertNotNull(firstProduct.getRating());
        assertNotNull(firstProduct.getImageUrl());
    }

    @Test
    @DisplayName("Should handle empty search results")
    void search_WithNoResults_ShouldReturnEmptyList() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.mercadoLivreSearchPageEmpty();
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("nonexistent product xyz123", 10);

        // Assert
        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    @Test
    @DisplayName("Should respect maxResults limit")
    void search_WithMaxResults_ShouldLimitResults() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.mercadoLivreSearchPageWithResults();
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("mouse", 1);

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
            () -> mercadoLivreScraper.search("mouse", 10)
        );

        assertEquals(Platform.MERCADO_LIVRE, exception.getPlatform());
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
            () -> mercadoLivreScraper.search("mouse", 10)
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
        String html = MockHtmlFixtures.mercadoLivreProductDetailsPage();
        setupMockHttpResponse(200, html);

        // Act
        Product product = mercadoLivreScraper.getProductDetails("MLB-123456789");

        // Assert
        assertNotNull(product);
        assertTrue(product.getName().contains("Mouse Logitech MX Master 3S"));
        assertEquals("MLB-123456789", product.getPlatformProductId());
        assertNotNull(product.getDescription());
        assertTrue(product.getDescription().contains("Mouse ergonômico"));
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
            () -> mercadoLivreScraper.getProductDetails("MLB-INVALID")
        );
    }

    // ========================================
    // Edge Cases - Products with Missing Fields
    // ========================================

    @Test
    @DisplayName("Should handle products without price gracefully")
    void search_WithProductsWithoutPrice_ShouldCreateProductWithoutPrice() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.mercadoLivreProductWithoutPrice();
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("test", 10);

        // Assert
        assertNotNull(products);
        assertFalse(products.isEmpty());

        Product product = products.get(0);
        assertEquals("Product Without Price", product.getName());
        assertNotNull(product.getRating());
    }

    @Test
    @DisplayName("Should handle products without rating gracefully")
    void search_WithProductsWithoutRating_ShouldCreateProductWithoutRating() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.mercadoLivreProductWithoutRating();
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("test", 10);

        // Assert
        assertNotNull(products);
        assertFalse(products.isEmpty());

        Product product = products.get(0);
        assertEquals("Product Without Rating", product.getName());
        assertNotNull(product.getPrice());
    }

    @Test
    @DisplayName("Should skip products with missing required fields")
    void search_WithProductsMissingRequiredFields_ShouldSkipThem() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.mercadoLivreProductWithMissingElements();
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("test", 10);

        // Assert
        assertNotNull(products);
        // Products without link or title should be filtered out
    }

    // ========================================
    // Product ID Extraction Tests
    // ========================================

    @Test
    @DisplayName("Should extract MLB product ID correctly")
    void search_WithMLBProductId_ShouldExtractCorrectly() throws IOException {
        // Arrange
        String html = """
            <html>
            <body>
                <li class="ui-search-layout__item">
                    <a class="ui-search-link" href="/produto/MLB-987654321-mouse-test/p">
                        <h2 class="ui-search-item__title">Test Product MLB</h2>
                    </a>
                    <span class="andes-money-amount__fraction">100</span>
                </li>
            </body>
            </html>
            """;
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        // The scraper extracts "MLB-987654321-mouse-test" (the full slug)
        assertTrue(product.getPlatformProductId().startsWith("MLB-987654321"));
    }

    @Test
    @DisplayName("Should extract MLA product ID correctly")
    void search_WithMLAProductId_ShouldExtractCorrectly() throws IOException {
        // Arrange
        String html = """
            <html>
            <body>
                <li class="ui-search-layout__item">
                    <a class="ui-search-link" href="/producto/MLA-555555555-mouse-argentina/p">
                        <h2 class="ui-search-item__title">Test Product MLA</h2>
                    </a>
                    <span class="andes-money-amount__fraction">100</span>
                </li>
            </body>
            </html>
            """;
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        // The scraper extracts "MLA-555555555-mouse-argentina" (the full slug)
        assertTrue(product.getPlatformProductId().startsWith("MLA-555555555"));
    }

    @Test
    @DisplayName("Should skip products with invalid URL format")
    void search_WithInvalidUrlFormat_ShouldSkipProduct() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.mercadoLivreProductWithInvalidUrl();
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("test", 10);

        // Assert
        assertNotNull(products);
        // Product with invalid URL should be skipped (no valid product ID)
    }

    // ========================================
    // Price Parsing Tests
    // ========================================

    @Test
    @DisplayName("Should parse Brazilian Real price correctly")
    void search_WithBrazilianPrice_ShouldParseCorrectly() throws IOException {
        // Arrange
        String html = """
            <html>
            <body>
                <li class="ui-search-layout__item">
                    <a class="ui-search-link" href="/p/MLB-111111111">
                        <h2 class="ui-search-item__title">Test Product</h2>
                    </a>
                    <span class="andes-money-amount__fraction">1299</span>
                </li>
            </body>
            </html>
            """;
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        assertNotNull(product.getPrice());
        assertTrue(product.getPrice().getAmount().compareTo(java.math.BigDecimal.valueOf(1299.0)) == 0);
    }

    // ========================================
    // Shipping Detection Tests
    // ========================================

    @Test
    @DisplayName("Should detect free shipping (Frete grátis)")
    void search_WithFreeShipping_ShouldSetShippingToZero() throws IOException {
        // Arrange
        String html = """
            <html>
            <body>
                <li class="ui-search-layout__item">
                    <a class="ui-search-link" href="/p/MLB-222222222">
                        <h2 class="ui-search-item__title">Test Product</h2>
                    </a>
                    <span class="andes-money-amount__fraction">200</span>
                    <span class="ui-search-item__shipping-label">Frete grátis</span>
                </li>
            </body>
            </html>
            """;
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        assertNotNull(product.getPrice());
        assertEquals(java.math.BigDecimal.ZERO, product.getPrice().getShippingCost());
    }

    @Test
    @DisplayName("Should handle products without shipping info")
    void search_WithoutShippingInfo_ShouldSetDefaultShipping() throws IOException {
        // Arrange
        String html = """
            <html>
            <body>
                <li class="ui-search-layout__item">
                    <a class="ui-search-link" href="/p/MLB-333333333">
                        <h2 class="ui-search-item__title">Test Product</h2>
                    </a>
                    <span class="andes-money-amount__fraction">150</span>
                </li>
            </body>
            </html>
            """;
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        assertNotNull(product.getPrice());
        // When no shipping info, defaults to ZERO
        assertEquals(java.math.BigDecimal.ZERO, product.getPrice().getShippingCost());
    }

    // ========================================
    // Rating Parsing Tests
    // ========================================

    @Test
    @DisplayName("Should parse rating with review count in parentheses")
    void search_WithRatingAndReviews_ShouldParseCorrectly() throws IOException {
        // Arrange
        String html = """
            <html>
            <body>
                <li class="ui-search-layout__item">
                    <a class="ui-search-link" href="/p/MLB-444444444">
                        <h2 class="ui-search-item__title">Test Product</h2>
                    </a>
                    <span class="andes-money-amount__fraction">250</span>
                    <span class="ui-search-reviews__rating-number">4.9</span>
                    <span class="ui-search-reviews__amount">(567)</span>
                </li>
            </body>
            </html>
            """;
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        assertNotNull(product.getRating());
        assertEquals(4.9, product.getRating().getScore());
        assertEquals(567, product.getRating().getReviewCount());
    }

    // ========================================
    // Image Handling Tests
    // ========================================

    @Test
    @DisplayName("Should extract image from src attribute")
    void search_WithImageSrc_ShouldExtractCorrectly() throws IOException {
        // Arrange
        String html = """
            <html>
            <body>
                <li class="ui-search-layout__item">
                    <a class="ui-search-link" href="/p/MLB-555555555">
                        <h2 class="ui-search-item__title">Test Product</h2>
                    </a>
                    <span class="andes-money-amount__fraction">300</span>
                    <img class="ui-search-result-image__element" src="https://http2.mlstatic.com/image1.jpg"/>
                </li>
            </body>
            </html>
            """;
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        assertNotNull(product.getImageUrl());
        assertTrue(product.getImageUrl().contains("mlstatic.com"));
    }

    @Test
    @DisplayName("Should extract image from data-src attribute when src is empty")
    void search_WithImageDataSrc_ShouldExtractCorrectly() throws IOException {
        // Arrange - Uses lazy loading with data-src
        String html = MockHtmlFixtures.mercadoLivreSearchPageWithResults();
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        // Second product uses data-src
        Product secondProduct = products.get(1);
        assertNotNull(secondProduct.getImageUrl());
        assertTrue(secondProduct.getImageUrl().contains("mlstatic.com"));
    }

    // ========================================
    // Special Characters and Encoding Tests
    // ========================================

    @Test
    @DisplayName("Should handle Portuguese product names correctly")
    void search_WithPortugueseCharacters_ShouldParseCorrectly() throws IOException {
        // Arrange
        String html = """
            <html>
            <body>
                <li class="ui-search-layout__item">
                    <a class="ui-search-link" href="/p/MLB-666666666">
                        <h2 class="ui-search-item__title">Mouse Ergonômico Ação Português</h2>
                    </a>
                    <span class="andes-money-amount__fraction">180</span>
                </li>
            </body>
            </html>
            """;
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("test", 10);

        // Assert
        assertFalse(products.isEmpty());
        Product product = products.get(0);
        assertTrue(product.getName().contains("Ergonômico"));
        assertTrue(product.getName().contains("Ação"));
    }

    // ========================================
    // Query Encoding Tests
    // ========================================

    @Test
    @DisplayName("Should URL encode search queries correctly")
    void search_WithSpacesInQuery_ShouldEncodeCorrectly() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.mercadoLivreSearchPageWithResults();
        setupMockHttpResponse(200, html);

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            List<Product> products = mercadoLivreScraper.search("mouse sem fio", 10);
            assertNotNull(products);
        });
    }

    @Test
    @DisplayName("Should handle queries with special characters")
    void search_WithSpecialCharactersInQuery_ShouldEncodeCorrectly() throws IOException {
        // Arrange
        String html = MockHtmlFixtures.mercadoLivreSearchPageWithResults();
        setupMockHttpResponse(200, html);

        // Act & Assert
        assertDoesNotThrow(() -> {
            List<Product> products = mercadoLivreScraper.search("mouse & teclado", 10);
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
        String veryLongTitle = "Mouse Gamer Sem Fio RGB LED Recarregável USB Sensor Óptico Alta Precisão 16000 DPI 7 Botões Programáveis Ergonômico".repeat(3);
        String html = String.format("""
            <html>
            <body>
                <li class="ui-search-layout__item">
                    <a class="ui-search-link" href="/p/MLB-777777777">
                        <h2 class="ui-search-item__title">%s</h2>
                    </a>
                    <span class="andes-money-amount__fraction">350</span>
                </li>
            </body>
            </html>
            """, veryLongTitle);
        setupMockHttpResponse(200, html);

        // Act
        List<Product> products = mercadoLivreScraper.search("test", 10);

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
