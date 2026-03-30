package com.gary.assistant.service;

import com.gary.assistant.dto.ComparisonResponse;
import com.gary.assistant.exception.ProductNotFoundException;
import com.gary.assistant.model.Product;
import com.gary.assistant.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ComparisonService {

    private static final Logger logger = LoggerFactory.getLogger(ComparisonService.class);

    private final ProductRepository productRepository;

    public ComparisonService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ComparisonResponse compareProducts(List<String> productIds) {
        logger.info("Comparing {} products", productIds.size());

        var products = productIds.stream()
            .map(id -> productRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ProductNotFoundException(id)))
            .toList();

        var bestPrice = findBestPrice(products);
        var bestValue = findBestValue(products);
        var metrics = generateMetrics(products);

        return new ComparisonResponse(
            products.stream().map(this::toProductResponse).toList(),
            bestPrice,
            bestValue,
            metrics
        );
    }

    private ComparisonResponse.BestDeal findBestPrice(List<Product> products) {
        return products.stream()
            .filter(p -> p.getPrice() != null)
            .min(Comparator.comparing(p -> p.getPrice().getTotal()))
            .map(p -> new ComparisonResponse.BestDeal(
                p.getId().toString(),
                p.getName(),
                p.getPrice().getTotal(),
                "Lowest total cost (price + shipping)"
            ))
            .orElse(null);
    }

    private ComparisonResponse.BestDeal findBestValue(List<Product> products) {
        return products.stream()
            .filter(p -> p.getPrice() != null && p.getRating() != null)
            .max(Comparator.comparing(this::calculateValueScore))
            .map(p -> new ComparisonResponse.BestDeal(
                p.getId().toString(),
                p.getName(),
                p.getPrice().getTotal(),
                "Best combination of price, rating, and reviews"
            ))
            .orElse(null);
    }

    private double calculateValueScore(Product product) {
        if (product.getPrice() == null || product.getRating() == null) {
            return 0.0;
        }

        var priceScore = 1000.0 / product.getPrice().getTotal().doubleValue();
        var ratingScore = product.getRating().getScore() != null ? product.getRating().getScore() : 0.0;
        var reviewScore = Math.log10(Math.max(1, product.getRating().getReviewCount()));

        return (priceScore * 0.4) + (ratingScore * 0.3) + (reviewScore * 0.3);
    }

    private List<ComparisonResponse.ComparisonMetric> generateMetrics(List<Product> products) {
        var metrics = new ArrayList<ComparisonResponse.ComparisonMetric>();

        var cheapest = products.stream()
            .filter(p -> p.getPrice() != null)
            .min(Comparator.comparing(p -> p.getPrice().getTotal()))
            .orElse(null);

        if (cheapest != null) {
            metrics.add(new ComparisonResponse.ComparisonMetric(
                "Lowest Price",
                cheapest.getPlatform().getDisplayName(),
                cheapest.getPrice().toString(),
                "Cheapest option available"
            ));
        }

        var highest = products.stream()
            .filter(p -> p.getRating() != null && p.getRating().getScore() != null)
            .max(Comparator.comparing(p -> p.getRating().getScore()))
            .orElse(null);

        if (highest != null) {
            metrics.add(new ComparisonResponse.ComparisonMetric(
                "Highest Rating",
                highest.getPlatform().getDisplayName(),
                highest.getRating().getScore() + "/5",
                "Best customer satisfaction"
            ));
        }

        var mostReviewed = products.stream()
            .filter(p -> p.getRating() != null)
            .max(Comparator.comparing(p -> p.getRating().getReviewCount()))
            .orElse(null);

        if (mostReviewed != null) {
            metrics.add(new ComparisonResponse.ComparisonMetric(
                "Most Reviews",
                mostReviewed.getPlatform().getDisplayName(),
                mostReviewed.getRating().getReviewCount() + " reviews",
                "Most validated by customers"
            ));
        }

        return metrics;
    }

    private com.gary.assistant.dto.ProductResponse toProductResponse(Product product) {
        // Reuse the conversion logic
        var priceInfo = product.getPrice() != null
            ? new com.gary.assistant.dto.ProductResponse.PriceInfo(
                product.getPrice().getAmount(),
                product.getPrice().getCurrency(),
                product.getPrice().getShippingCost(),
                product.getPrice().getTotal()
            )
            : null;

        var ratingInfo = product.getRating() != null
            ? new com.gary.assistant.dto.ProductResponse.RatingInfo(
                product.getRating().getScore(),
                product.getRating().getReviewCount(),
                product.getRating().isReliable()
            )
            : null;

        return new com.gary.assistant.dto.ProductResponse(
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
