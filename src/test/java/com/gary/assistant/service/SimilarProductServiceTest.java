package com.gary.assistant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimilarProductServiceTest {

    private SimilarProductService service;

    @BeforeEach
    void setUp() {
        service = new SimilarProductService();
    }

    @Test
    void generateSimilarQueries_ShouldCreateProgressivelyBroaderQueries() {
        String query = "Logitech MX Master 3S Mouse";

        List<String> queries = service.generateSimilarQueries(query);

        assertNotNull(queries);
        assertFalse(queries.isEmpty());
        assertEquals(query, queries.get(0)); // First query should be original

        System.out.println("Generated queries for: " + query);
        queries.forEach(q -> System.out.println("  - " + q));

        // Should contain progressively broader queries
        assertTrue(queries.size() >= 2);
    }

    @Test
    void generateSimilarQueries_ShouldHandleSimpleQuery() {
        String query = "mouse";

        List<String> queries = service.generateSimilarQueries(query);

        assertNotNull(queries);
        assertTrue(queries.contains("mouse"));
    }

    @Test
    void calculateSimilarity_ShouldReturnHighScoreForSimilarProducts() {
        String name1 = "Logitech MX Master 3S Wireless Mouse";
        String name2 = "Logitech MX Master 3 Wireless Mouse";

        double similarity = service.calculateSimilarity(name1, name2);

        System.out.println("Similarity between:");
        System.out.println("  " + name1);
        System.out.println("  " + name2);
        System.out.println("  Score: " + similarity);

        assertTrue(similarity > 0.5, "Similar products should have high similarity score");
    }

    @Test
    void calculateSimilarity_ShouldReturnLowScoreForDifferentProducts() {
        String name1 = "Logitech MX Master 3S Mouse";
        String name2 = "Apple MacBook Pro Laptop";

        double similarity = service.calculateSimilarity(name1, name2);

        System.out.println("Similarity between:");
        System.out.println("  " + name1);
        System.out.println("  " + name2);
        System.out.println("  Score: " + similarity);

        assertTrue(similarity < 0.3, "Different products should have low similarity score");
    }

    @Test
    void isRelevant_ShouldIdentifyRelevantProducts() {
        String query = "Logitech MX Master";
        String productName = "Mouse Logitech MX Master 3S Wireless Gaming";

        boolean relevant = service.isRelevant(productName, query, 0.3);

        assertTrue(relevant, "Product should be relevant to query");
    }

    @Test
    void isRelevant_ShouldRejectIrrelevantProducts() {
        String query = "Logitech Mouse";
        String productName = "Samsung Galaxy S23 Smartphone";

        boolean relevant = service.isRelevant(productName, query, 0.3);

        assertFalse(relevant, "Product should not be relevant to query");
    }

    @Test
    void extractKeyFeatures_ShouldIdentifyBrandsAndTypes() {
        String productName = "Logitech MX Master 3S Wireless Mouse";

        List<String> features = service.extractKeyFeatures(productName);

        System.out.println("Extracted features from: " + productName);
        features.forEach(f -> System.out.println("  - " + f));

        assertFalse(features.isEmpty());
        assertTrue(features.contains("logitech"));
        assertTrue(features.contains("mouse"));
    }

    @Test
    void generateSimilarQueries_ComplexExample() {
        String query = "Apple MacBook Pro 16 M3 Max";

        List<String> queries = service.generateSimilarQueries(query);

        System.out.println("\nFallback strategy for: " + query);
        for (int i = 0; i < queries.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + queries.get(i));
        }

        assertNotNull(queries);
        assertTrue(queries.size() >= 2);
    }
}
