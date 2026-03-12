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
public class MercadoLivreScraper implements ProductScraper {

    private static final Logger logger = LoggerFactory.getLogger(MercadoLivreScraper.class);

    @Override
    public Platform getPlatform() {
        return Platform.MERCADO_LIVRE;
    }

    @Override
    @RateLimiter(name = "mercadoLivreScraper", fallbackMethod = "searchFallback")
    @CircuitBreaker(name = "scraperCircuitBreaker", fallbackMethod = "searchFallback")
    public List<Product> search(String query, int maxResults) {
        logger.info("Searching Mercado Livre for: {}", query);

        // TODO: Implement actual Mercado Livre scraping
        throw new ScraperException(
            Platform.MERCADO_LIVRE,
            "Mercado Livre scraping not yet implemented"
        );
    }

    private List<Product> searchFallback(String query, int maxResults, Exception e) {
        logger.warn("Mercado Livre search fallback triggered for query '{}': {}", query, e.getMessage());
        return List.of();
    }

    @Override
    @RateLimiter(name = "mercadoLivreScraper", fallbackMethod = "getProductDetailsFallback")
    @CircuitBreaker(name = "scraperCircuitBreaker", fallbackMethod = "getProductDetailsFallback")
    public Product getProductDetails(String productId) {
        logger.info("Fetching Mercado Livre product details for: {}", productId);

        // TODO: Implement actual product details fetching
        throw new ScraperException(
            Platform.MERCADO_LIVRE,
            "Product details fetching not yet implemented"
        );
    }

    private Product getProductDetailsFallback(String productId, Exception e) {
        logger.warn("Mercado Livre product details fallback triggered for ID '{}': {}", productId, e.getMessage());
        throw new ScraperException(Platform.MERCADO_LIVRE, "Service temporarily unavailable");
    }

    @Override
    public boolean isAvailable() {
        // TODO: Implement health check
        return false;
    }
}
