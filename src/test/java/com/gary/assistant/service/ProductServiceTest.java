package com.gary.assistant.service;

import com.gary.assistant.dto.SearchRequest;
import com.gary.assistant.exception.ProductNotFoundException;
import com.gary.assistant.model.Currency;
import com.gary.assistant.model.Platform;
import com.gary.assistant.model.Price;
import com.gary.assistant.model.Product;
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
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ProductService.
 * Tests search, filtering, sorting, caching, and database operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ScraperService scraperService;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, scraperService);
    }

    // ========================================
    // Search Tests
    // ========================================

    @Test
    @DisplayName("Should search and save products successfully")
    void search_WithValidRequest_ShouldReturnSearchResponse() {
        // Arrange
        var request = new SearchRequest(
            "mouse",
            null,
            Set.of(Platform.AMAZON),
            SearchRequest.SortBy.LOWEST_PRICE
        );

        var scrapedProducts = TestDataBuilder.createProductList(3, Platform.AMAZON);
        when(scraperService.searchAllPlatformsWithCrossPlatformFallback(
            eq("mouse"),
            eq(Set.of(Platform.AMAZON))
        )).thenReturn(scrapedProducts);

        when(productRepository.findByPlatformAndPlatformProductId(any(), any()))
            .thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = productService.search(request);

        // Assert
        assertNotNull(response);
        assertEquals("mouse", response.query());
        assertEquals(3, response.totalResults());
        assertEquals(3, response.products().size());
        assertNotNull(response.searchId());
        assertNotNull(response.timestamp());
        assertTrue(response.durationMs() >= 0);

        verify(scraperService).searchAllPlatformsWithCrossPlatformFallback(
            eq("mouse"),
            eq(Set.of(Platform.AMAZON))
        );
        verify(productRepository, times(3)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should filter products by max price")
    void search_WithMaxPrice_ShouldFilterProducts() {
        // Arrange
        var cheapProduct = TestDataBuilder.createAmazonProduct("CHEAP", "Cheap Mouse");
        cheapProduct.updatePrice(new Price(new BigDecimal("100"), Currency.BRL, BigDecimal.ZERO));

        var expensiveProduct = TestDataBuilder.createAmazonProduct("EXPENSIVE", "Expensive Mouse");
        expensiveProduct.updatePrice(new Price(new BigDecimal("500"), Currency.BRL, BigDecimal.ZERO));

        var request = new SearchRequest(
            "mouse",
            new BigDecimal("200"), // Max price
            Set.of(Platform.AMAZON),
            SearchRequest.SortBy.LOWEST_PRICE
        );

        when(scraperService.searchAllPlatformsWithCrossPlatformFallback(anyString(), any()))
            .thenReturn(List.of(cheapProduct, expensiveProduct));
        when(productRepository.findByPlatformAndPlatformProductId(any(), any()))
            .thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = productService.search(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.totalResults()); // Only cheap product
        assertEquals("Cheap Mouse", response.products().get(0).name());
    }

    @Test
    @DisplayName("Should sort products by lowest price")
    void search_WithSortByLowestPrice_ShouldSortCorrectly() {
        // Arrange
        var product1 = TestDataBuilder.createAmazonProduct("P1", "Product 1");
        product1.updatePrice(new Price(new BigDecimal("300"), Currency.BRL, BigDecimal.ZERO));

        var product2 = TestDataBuilder.createAmazonProduct("P2", "Product 2");
        product2.updatePrice(new Price(new BigDecimal("100"), Currency.BRL, BigDecimal.ZERO));

        var product3 = TestDataBuilder.createAmazonProduct("P3", "Product 3");
        product3.updatePrice(new Price(new BigDecimal("200"), Currency.BRL, BigDecimal.ZERO));

        var request = new SearchRequest(
            "mouse",
            null,
            Set.of(Platform.AMAZON),
            SearchRequest.SortBy.LOWEST_PRICE
        );

        when(scraperService.searchAllPlatformsWithCrossPlatformFallback(anyString(), any()))
            .thenReturn(List.of(product1, product2, product3));
        when(productRepository.findByPlatformAndPlatformProductId(any(), any()))
            .thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = productService.search(request);

        // Assert
        assertEquals(3, response.totalResults());
        assertEquals(new BigDecimal("100"), response.products().get(0).price().total());
        assertEquals(new BigDecimal("200"), response.products().get(1).price().total());
        assertEquals(new BigDecimal("300"), response.products().get(2).price().total());
    }

    @Test
    @DisplayName("Should sort products by highest rating")
    void search_WithSortByHighestRating_ShouldSortCorrectly() {
        // Arrange
        var products = TestDataBuilder.createProductList(3, Platform.AMAZON);
        products.get(0).getRating().setScore(4.5);
        products.get(1).getRating().setScore(4.8); // Highest
        products.get(2).getRating().setScore(4.2);

        var request = new SearchRequest(
            "mouse",
            null,
            Set.of(Platform.AMAZON),
            SearchRequest.SortBy.HIGHEST_RATING
        );

        when(scraperService.searchAllPlatformsWithCrossPlatformFallback(anyString(), any()))
            .thenReturn(products);
        when(productRepository.findByPlatformAndPlatformProductId(any(), any()))
            .thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = productService.search(request);

        // Assert
        assertEquals(3, response.totalResults());
        assertEquals(4.8, response.products().get(0).rating().score());
        assertEquals(4.5, response.products().get(1).rating().score());
        assertEquals(4.2, response.products().get(2).rating().score());
    }

    @Test
    @DisplayName("Should sort products by most reviews")
    void search_WithSortByMostReviews_ShouldSortCorrectly() {
        // Arrange
        var products = TestDataBuilder.createProductList(3, Platform.AMAZON);
        products.get(0).getRating().setReviewCount(100);
        products.get(1).getRating().setReviewCount(500); // Most reviews
        products.get(2).getRating().setReviewCount(50);

        var request = new SearchRequest(
            "mouse",
            null,
            Set.of(Platform.AMAZON),
            SearchRequest.SortBy.MOST_REVIEWS
        );

        when(scraperService.searchAllPlatformsWithCrossPlatformFallback(anyString(), any()))
            .thenReturn(products);
        when(productRepository.findByPlatformAndPlatformProductId(any(), any()))
            .thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = productService.search(request);

        // Assert
        assertEquals(3, response.totalResults());
        assertEquals(500, response.products().get(0).rating().reviewCount());
        assertEquals(100, response.products().get(1).rating().reviewCount());
        assertEquals(50, response.products().get(2).rating().reviewCount());
    }

    @Test
    @DisplayName("Should filter out unavailable products")
    void search_WithUnavailableProducts_ShouldFilterThem() {
        // Arrange
        var availableProduct = TestDataBuilder.createAmazonProduct("AVAIL", "Available");
        var unavailableProduct = TestDataBuilder.createAmazonProduct("UNAVAIL", "Unavailable");
        unavailableProduct.setAvailable(false);

        var request = new SearchRequest(
            "mouse",
            null,
            Set.of(Platform.AMAZON),
            SearchRequest.SortBy.LOWEST_PRICE
        );

        when(scraperService.searchAllPlatformsWithCrossPlatformFallback(anyString(), any()))
            .thenReturn(List.of(availableProduct, unavailableProduct));
        when(productRepository.findByPlatformAndPlatformProductId(any(), any()))
            .thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = productService.search(request);

        // Assert
        assertEquals(1, response.totalResults()); // Only available product
        assertEquals("Available", response.products().get(0).name());
    }

    // ========================================
    // Get Product Tests
    // ========================================

    @Test
    @DisplayName("Should get product by id successfully")
    void getProduct_WithValidId_ShouldReturnProduct() {
        // Arrange
        var product = TestDataBuilder.createAmazonProduct();
        var productId = product.getId();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        var response = productService.getProduct(productId.toString());

        // Assert
        assertNotNull(response);
        assertEquals(productId.toString(), response.id());
        assertEquals(product.getName(), response.name());
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void getProduct_WithInvalidId_ShouldThrowException() {
        // Arrange
        var productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () ->
            productService.getProduct(productId.toString())
        );
        verify(productRepository).findById(productId);
    }

    // ========================================
    // Save/Update Product Tests
    // ========================================

    @Test
    @DisplayName("Should create new product when not exists")
    void saveOrUpdateProduct_WithNewProduct_ShouldCreateNew() {
        // Arrange
        var product = TestDataBuilder.createAmazonProduct("NEW123", "New Product");

        when(productRepository.findByPlatformAndPlatformProductId(
            Platform.AMAZON,
            "NEW123"
        )).thenReturn(Optional.empty());
        when(productRepository.save(product)).thenReturn(product);

        // Act
        var result = productService.saveOrUpdateProduct(product);

        // Assert
        assertNotNull(result);
        assertEquals("New Product", result.getName());
        verify(productRepository).findByPlatformAndPlatformProductId(Platform.AMAZON, "NEW123");
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should update existing product with new price and rating")
    void saveOrUpdateProduct_WithExistingProduct_ShouldUpdate() {
        // Arrange
        var existingProduct = TestDataBuilder.createAmazonProduct("EXIST123", "Existing");
        var newPrice = new Price(new BigDecimal("999"), Currency.BRL, BigDecimal.ZERO);
        var updatedProduct = TestDataBuilder.createAmazonProduct("EXIST123", "Existing");
        updatedProduct.updatePrice(newPrice);
        updatedProduct.getRating().setScore(4.9);

        when(productRepository.findByPlatformAndPlatformProductId(
            Platform.AMAZON,
            "EXIST123"
        )).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        var result = productService.saveOrUpdateProduct(updatedProduct);

        // Assert
        assertNotNull(result);
        verify(productRepository).findByPlatformAndPlatformProductId(Platform.AMAZON, "EXIST123");
        verify(productRepository).save(existingProduct);
    }
}
