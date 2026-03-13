package com.gary.assistant.service;

import com.gary.assistant.config.FallbackConfig;
import com.gary.assistant.model.Platform;
import com.gary.assistant.model.Product;
import com.gary.assistant.scraper.ProductScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private final SimilarProductService similarProductService;
    private final FallbackConfig fallbackConfig;

    public ScraperService(List<ProductScraper> scraperList,
                          SimilarProductService similarProductService,
                          FallbackConfig fallbackConfig) {
        this.scrapers = scraperList.stream()
            .collect(Collectors.toMap(
                ProductScraper::getPlatform,
                Function.identity()
            ));
        this.pendingSearches = new ConcurrentHashMap<>();
        this.similarProductService = similarProductService;
        this.fallbackConfig = fallbackConfig;
        logger.info("Initialized scrapers for platforms: {}", scrapers.keySet());
    }

    /**
     * Searches all platforms concurrently with fallback support.
     * If results are insufficient, tries alternative search queries.
     */
    public List<Product> searchAllPlatforms(String query, Set<Platform> platforms) {
        logger.info("Searching query '{}' across platforms: {}", query, platforms);

        List<Product> results = searchAllPlatformsInternal(query, platforms);

        // Apply fallback strategy if enabled and results are insufficient
        if (fallbackConfig.isEnabled() && results.size() < fallbackConfig.getMinResultsThreshold()) {
            logger.info("Primary search returned {} results (threshold: {}), applying fallback strategy",
                results.size(), fallbackConfig.getMinResultsThreshold());
            results = searchWithFallback(query, platforms, results);
        }

        return results;
    }

    /**
     * Internal method to search all platforms without fallback.
     */
    private List<Product> searchAllPlatformsInternal(String query, Set<Platform> platforms) {
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
     * Applies fallback strategy by trying alternative queries.
     */
    private List<Product> searchWithFallback(String originalQuery, Set<Platform> platforms, List<Product> initialResults) {
        Set<Product> allResults = new LinkedHashSet<>(initialResults);
        List<String> fallbackQueries = similarProductService.generateSimilarQueries(originalQuery);

        logger.info("Generated {} fallback queries: {}", fallbackQueries.size(), fallbackQueries);

        int attempts = 0;
        for (String fallbackQuery : fallbackQueries) {
            if (fallbackQuery.equals(originalQuery)) {
                continue; // Skip original query
            }

            if (attempts >= fallbackConfig.getMaxAttempts()) {
                logger.info("Reached max fallback attempts ({})", fallbackConfig.getMaxAttempts());
                break;
            }

            if (allResults.size() >= fallbackConfig.getMaxTotalResults()) {
                logger.info("Reached max total results ({})", fallbackConfig.getMaxTotalResults());
                break;
            }

            logger.info("Fallback attempt {}: searching with query '{}'", attempts + 1, fallbackQuery);

            List<Product> fallbackResults = searchAllPlatformsInternal(fallbackQuery, platforms);
            List<Product> relevantResults = filterRelevantProducts(fallbackResults, originalQuery);

            logger.info("Fallback query '{}' returned {} results ({} relevant)",
                fallbackQuery, fallbackResults.size(), relevantResults.size());

            allResults.addAll(relevantResults);
            attempts++;

            if (allResults.size() >= fallbackConfig.getMinResultsThreshold()) {
                logger.info("Sufficient results found after {} fallback attempts", attempts);
                break;
            }
        }

        logger.info("Fallback strategy completed: {} total results", allResults.size());
        return new ArrayList<>(allResults).subList(0, Math.min(allResults.size(), fallbackConfig.getMaxTotalResults()));
    }

    /**
     * Filters products to keep only those similar to the original query.
     */
    private List<Product> filterRelevantProducts(List<Product> products, String originalQuery) {
        return products.stream()
            .filter(product -> similarProductService.isRelevant(
                product.getName(),
                originalQuery,
                fallbackConfig.getSimilarityThreshold()
            ))
            .toList();
    }

    /**
     * Searches all platforms with cross-platform fallback.
     * If one platform fails, automatically tries all other platforms.
     */
    public List<Product> searchAllPlatformsWithCrossPlatformFallback(String query, Set<Platform> platforms) {
        List<Product> results = searchAllPlatforms(query, platforms);

        if (fallbackConfig.isCrossPlatformEnabled() && results.size() < fallbackConfig.getMinResultsThreshold()) {
            logger.info("Cross-platform fallback: expanding search to all available platforms");

            Set<Platform> allPlatforms = scrapers.keySet();
            Set<Platform> additionalPlatforms = new HashSet<>(allPlatforms);
            additionalPlatforms.removeAll(platforms);

            if (!additionalPlatforms.isEmpty()) {
                logger.info("Searching additional platforms: {}", additionalPlatforms);
                List<Product> additionalResults = searchAllPlatformsInternal(query, additionalPlatforms);
                results = new ArrayList<>(results);
                results.addAll(additionalResults);
            }
        }

        return results;
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
