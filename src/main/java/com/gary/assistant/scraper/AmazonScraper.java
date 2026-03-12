package com.gary.assistant.scraper;

import com.gary.assistant.exception.ScraperException;
import com.gary.assistant.model.Platform;
import com.gary.assistant.model.Product;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AmazonScraper implements ProductScraper {

    private static final Logger logger = LoggerFactory.getLogger(AmazonScraper.class);

    @Override
    public Platform getPlatform() {
        return Platform.AMAZON;
    }

    @Override
    @RateLimiter(name = "amazonScraper", fallbackMethod = "searchFallback")
    @CircuitBreaker(name = "scraperCircuitBreaker", fallbackMethod = "searchFallback")
    public List<Product> search(String query, int maxResults) {
        logger.info("Searching Amazon for: {}", query);

        // TODO: Implement actual Amazon scraping
        // For now, this is a placeholder that will integrate with the amazon-shopping skill
        throw new ScraperException(
            Platform.AMAZON,
            "Amazon scraping not yet implemented. Use amazon-shopping skill for now."
        );
    }

    private List<Product> searchFallback(String query, int maxResults, Exception e) {
        logger.warn("Amazon search fallback triggered for query '{}': {}", query, e.getMessage());
        return List.of();
    }

    @Override
    @RateLimiter(name = "amazonScraper", fallbackMethod = "getProductDetailsFallback")
    @CircuitBreaker(name = "scraperCircuitBreaker", fallbackMethod = "getProductDetailsFallback")
    public Product getProductDetails(String productId) {
        logger.info("Fetching Amazon product details for ASIN: {}", productId);

        // TODO: Implement actual product details fetching
        throw new ScraperException(
            Platform.AMAZON,
            "Product details fetching not yet implemented"
        );
    }

    private Product getProductDetailsFallback(String productId, Exception e) {
        logger.warn("Amazon product details fallback triggered for ID '{}': {}", productId, e.getMessage());
        throw new ScraperException(Platform.AMAZON, "Service temporarily unavailable");
    }

    @Override
    public boolean isAvailable() {
        // TODO: Implement health check
        return false;
    }
}
