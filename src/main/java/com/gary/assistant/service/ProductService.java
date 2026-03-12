package com.gary.assistant.service;

import com.gary.assistant.dto.ProductResponse;
import com.gary.assistant.dto.SearchRequest;
import com.gary.assistant.dto.SearchResponse;
import com.gary.assistant.exception.ProductNotFoundException;
import com.gary.assistant.model.Product;
import com.gary.assistant.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ScraperService scraperService;

    public ProductService(ProductRepository productRepository, ScraperService scraperService) {
        this.productRepository = productRepository;
        this.scraperService = scraperService;
    }

    @Transactional
    public SearchResponse search(SearchRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("Starting search for query: {}", request.query());

        var products = scraperService.searchAllPlatforms(request.query(), request.platforms());

        var savedProducts = products.stream()
            .map(this::saveOrUpdateProduct)
            .toList();

        var filteredProducts = filterAndSort(savedProducts, request);
        var responses = filteredProducts.stream()
            .map(this::toResponse)
            .toList();

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Search completed in {}ms, found {} products", duration, responses.size());

        return new SearchResponse(
            UUID.randomUUID().toString(),
            request.query(),
            responses,
            responses.size(),
            Instant.now(),
            duration
        );
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProduct(String id) {
        logger.info("Fetching product with id: {}", id);
        var product = productRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ProductNotFoundException(id));
        return toResponse(product);
    }

    @Transactional
    public Product saveOrUpdateProduct(Product product) {
        var existing = productRepository.findByPlatformAndPlatformProductId(
            product.getPlatform(),
            product.getPlatformProductId()
        );

        if (existing.isPresent()) {
            var existingProduct = existing.get();
            if (product.getPrice() != null) {
                existingProduct.updatePrice(product.getPrice());
            }
            if (product.getRating() != null) {
                existingProduct.updateRating(product.getRating());
            }
            return productRepository.save(existingProduct);
        }

        return productRepository.save(product);
    }

    private List<Product> filterAndSort(List<Product> products, SearchRequest request) {
        var filtered = products.stream()
            .filter(Product::isAvailable)
            .filter(p -> request.maxPrice() == null ||
                        (p.getPrice() != null && p.getPrice().getTotal().compareTo(request.maxPrice()) <= 0))
            .toList();

        return switch (request.sortBy()) {
            case LOWEST_PRICE -> filtered.stream()
                .sorted(Comparator.comparing(p -> p.getPrice() != null ? p.getPrice().getTotal() : null,
                    Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
            case HIGHEST_RATING -> filtered.stream()
                .sorted(Comparator.comparing(p -> p.getRating() != null ? p.getRating().getScore() : null,
                    Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
            case MOST_REVIEWS -> filtered.stream()
                .sorted(Comparator.comparing(p -> p.getRating() != null ? p.getRating().getReviewCount() : null,
                    Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        };
    }

    private ProductResponse toResponse(Product product) {
        var priceInfo = product.getPrice() != null
            ? new ProductResponse.PriceInfo(
                product.getPrice().getAmount(),
                product.getPrice().getCurrency(),
                product.getPrice().getShippingCost(),
                product.getPrice().getTotal()
            )
            : null;

        var ratingInfo = product.getRating() != null
            ? new ProductResponse.RatingInfo(
                product.getRating().getScore(),
                product.getRating().getReviewCount(),
                product.getRating().isReliable()
            )
            : null;

        return new ProductResponse(
            product.getId().toString(),
            product.getName(),
            product.getDescription(),
            product.getPlatform(),
            product.getPlatformProductId(),
            product.getUrl(),
            priceInfo,
            ratingInfo,
            product.getImageUrl(),
            product.isAvailable(),
            product.getUpdatedAt()
        );
    }
}
