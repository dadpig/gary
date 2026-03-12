package com.gary.assistant.service;

import com.gary.assistant.model.Platform;
import com.gary.assistant.model.Product;
import com.gary.assistant.scraper.ProductScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ScraperService {

    private static final Logger logger = LoggerFactory.getLogger(ScraperService.class);
    private static final int DEFAULT_MAX_RESULTS = 10;

    private final Map<Platform, ProductScraper> scrapers;
    private final ConcurrentHashMap<String, CompletableFuture<List<Product>>> pendingSearches;

    public ScraperService(List<ProductScraper> scraperList) {
        this.scrapers = scraperList.stream()
            .collect(Collectors.toMap(
                ProductScraper::getPlatform,
                Function.identity()
            ));
        this.pendingSearches = new ConcurrentHashMap<>();
        logger.info("Initialized scrapers for platforms: {}", scrapers.keySet());
    }

    /**
     * Searches all platforms concurrently using CompletableFuture with virtual threads.
     * Virtual threads (enabled via AsyncConfig) allow efficient concurrent I/O operations.
     * Request coalescing prevents duplicate concurrent requests.
     */
    public List<Product> searchAllPlatforms(String query, Set<Platform> platforms) {
        logger.info("Searching query '{}' across platforms: {}", query, platforms);

        // Create async tasks with virtual threads for each platform
        var futures = platforms.stream()
            .filter(scrapers::containsKey)
            .map(platform -> searchPlatformWithCoalescingAsync(platform, query))
            .toList();

        // Wait for all searches to complete and collect results
        return futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .toList();
    }

    /**
     * Searches a platform asynchronously with request coalescing.
     * If multiple requests for the same platform+query arrive simultaneously,
     * only one actual search is performed and the result is shared.
     */
    private CompletableFuture<List<Product>> searchPlatformWithCoalescingAsync(Platform platform, String query) {
        String cacheKey = platform + ":" + query;

        // Check if there's already a pending search for this platform+query
        return pendingSearches.computeIfAbsent(cacheKey, k -> {
            logger.debug("Starting new search for platform {} and query '{}'", platform, query);

            return CompletableFuture.supplyAsync(() -> searchPlatform(platform, query))
                .whenComplete((result, ex) -> {
                    // Clean up completed search from pending map
                    pendingSearches.remove(k);
                    if (ex != null) {
                        logger.error("Search failed for platform {} and query '{}': {}",
                            platform, query, ex.getMessage());
                    }
                });
        }).thenApply(result -> {
            if (pendingSearches.containsKey(cacheKey)) {
                logger.debug("Request coalesced for platform {} and query '{}'", platform, query);
            }
            return result;
        });
    }

    public List<Product> searchPlatform(Platform platform, String query) {
        var scraper = scrapers.get(platform);
        if (scraper == null) {
            logger.warn("No scraper available for platform: {}", platform);
            return List.of();
        }

        try {
            return scraper.search(query, DEFAULT_MAX_RESULTS);
        } catch (Exception e) {
            logger.error("Error searching {} for query '{}': {}",
                platform, query, e.getMessage(), e);
            return List.of();
        }
    }

    public Product getProductDetails(Platform platform, String productId) {
        var scraper = scrapers.get(platform);
        if (scraper == null) {
            throw new IllegalArgumentException("No scraper available for platform: " + platform);
        }

        return scraper.getProductDetails(productId);
    }

    public Map<Platform, Boolean> getScraperStatus() {
        return scrapers.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().isAvailable()
            ));
    }
}
