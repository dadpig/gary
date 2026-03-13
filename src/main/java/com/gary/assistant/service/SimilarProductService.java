package com.gary.assistant.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for generating similar product search queries and analyzing product similarity.
 */
@Service
public class SimilarProductService {

    private static final Logger logger = LoggerFactory.getLogger(SimilarProductService.class);

    // Common brands to preserve in searches
    private static final Set<String> COMMON_BRANDS = Set.of(
        "logitech", "microsoft", "apple", "samsung", "sony", "lg", "dell", "hp",
        "lenovo", "asus", "acer", "razer", "corsair", "hyperx", "steelseries",
        "nike", "adidas", "puma", "jbl", "bose", "sennheiser", "philips"
    );

    // Words to remove for broader searches
    private static final Set<String> STOP_WORDS = Set.of(
        "o", "a", "os", "as", "de", "do", "da", "dos", "das", "em", "no", "na",
        "com", "para", "por", "the", "and", "or", "of", "in", "on", "at", "to"
    );

    // Common product type words
    private static final Set<String> PRODUCT_TYPES = Set.of(
        "mouse", "teclado", "keyboard", "headset", "fone", "monitor", "notebook",
        "laptop", "smartphone", "tablet", "cadeira", "chair", "mesa", "desk",
        "webcam", "camera", "microfone", "microphone", "speaker", "caixa"
    );

    /**
     * Generates alternative search queries for fallback searches.
     * Returns queries in order of specificity (most specific first).
     */
    public List<String> generateSimilarQueries(String originalQuery) {
        logger.debug("Generating similar queries for: {}", originalQuery);

        List<String> queries = new ArrayList<>();
        queries.add(originalQuery); // Original query first

        String normalized = normalizeQuery(originalQuery);
        QueryComponents components = extractComponents(normalized);

        // Strategy 1: Remove exact model numbers (e.g., "MX Master 3S" -> "MX Master")
        if (components.modelNumber != null) {
            queries.add(components.brand + " " + components.baseModel);
        }

        // Strategy 2: Brand + product type only (e.g., "Logitech mouse")
        if (components.brand != null && components.productType != null) {
            queries.add(components.brand + " " + components.productType);
        }

        // Strategy 3: Remove brand, keep model and type (e.g., "MX Master mouse")
        if (components.baseModel != null && components.productType != null) {
            queries.add(components.baseModel + " " + components.productType);
        }

        // Strategy 4: Product type only (broadest search)
        if (components.productType != null) {
            queries.add(components.productType);
        }

        // Strategy 5: Remove special characters and extra spaces
        String simplified = simplifyQuery(originalQuery);
        if (!simplified.equals(originalQuery)) {
            queries.add(simplified);
        }

        // Remove duplicates while preserving order
        return queries.stream()
            .distinct()
            .filter(q -> q != null && !q.isBlank())
            .toList();
    }

    /**
     * Extracts key components from a product query.
     */
    private QueryComponents extractComponents(String query) {
        QueryComponents components = new QueryComponents();

        String lowerQuery = query.toLowerCase();

        // Extract brand
        for (String brand : COMMON_BRANDS) {
            if (lowerQuery.contains(brand)) {
                components.brand = brand;
                break;
            }
        }

        // Extract product type
        for (String type : PRODUCT_TYPES) {
            if (lowerQuery.contains(type)) {
                components.productType = type;
                break;
            }
        }

        // Extract model number (alphanumeric patterns like "3S", "G502", "MX3")
        Pattern modelPattern = Pattern.compile("\\b([A-Z]{1,3}\\d{1,4}[A-Z]?|\\d{3,4}[A-Z]?)\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = modelPattern.matcher(query);
        if (matcher.find()) {
            components.modelNumber = matcher.group(1);
        }

        // Extract base model (e.g., "MX Master" from "MX Master 3S")
        if (components.modelNumber != null) {
            String withoutModel = query.replaceAll("\\s*" + components.modelNumber + "\\s*", " ").trim();
            components.baseModel = withoutModel;
        } else {
            components.baseModel = query;
        }

        return components;
    }

    /**
     * Normalizes query by removing extra whitespace and common punctuation.
     */
    private String normalizeQuery(String query) {
        return query.replaceAll("[\\[\\](){}]", " ")
                   .replaceAll("\\s+", " ")
                   .trim();
    }

    /**
     * Simplifies query by removing stop words and special characters.
     */
    private String simplifyQuery(String query) {
        String[] words = query.toLowerCase().split("\\s+");
        return Arrays.stream(words)
            .filter(word -> !STOP_WORDS.contains(word))
            .filter(word -> word.length() > 2)
            .collect(Collectors.joining(" "));
    }

    /**
     * Calculates similarity score between two product names (0.0 to 1.0).
     * Uses a simple keyword overlap algorithm.
     */
    public double calculateSimilarity(String name1, String name2) {
        if (name1 == null || name2 == null) {
            return 0.0;
        }

        Set<String> words1 = tokenize(name1.toLowerCase());
        Set<String> words2 = tokenize(name2.toLowerCase());

        if (words1.isEmpty() || words2.isEmpty()) {
            return 0.0;
        }

        // Calculate Jaccard similarity
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        double jaccard = (double) intersection.size() / union.size();

        // Boost score if brand matches
        boolean brandMatch = COMMON_BRANDS.stream()
            .anyMatch(brand -> words1.contains(brand) && words2.contains(brand));

        // Boost score if product type matches
        boolean typeMatch = PRODUCT_TYPES.stream()
            .anyMatch(type -> words1.contains(type) && words2.contains(type));

        double boost = (brandMatch ? 0.2 : 0.0) + (typeMatch ? 0.1 : 0.0);

        return Math.min(1.0, jaccard + boost);
    }

    /**
     * Tokenizes a string into meaningful words.
     */
    private Set<String> tokenize(String text) {
        return Arrays.stream(text.split("\\W+"))
            .filter(word -> !word.isEmpty())
            .filter(word -> !STOP_WORDS.contains(word))
            .filter(word -> word.length() > 1)
            .collect(Collectors.toSet());
    }

    /**
     * Checks if a product name is relevant to the search query.
     * Returns true if similarity score exceeds threshold.
     */
    public boolean isRelevant(String productName, String query, double threshold) {
        double similarity = calculateSimilarity(productName, query);
        return similarity >= threshold;
    }

    /**
     * Extracts key features from product names for comparison.
     */
    public List<String> extractKeyFeatures(String productName) {
        List<String> features = new ArrayList<>();

        // Extract brand
        String lowerName = productName.toLowerCase();
        COMMON_BRANDS.stream()
            .filter(lowerName::contains)
            .forEach(features::add);

        // Extract product type
        PRODUCT_TYPES.stream()
            .filter(lowerName::contains)
            .forEach(features::add);

        // Extract model numbers
        Pattern modelPattern = Pattern.compile("\\b([A-Z0-9]{2,})\\b");
        Matcher matcher = modelPattern.matcher(productName);
        while (matcher.find()) {
            features.add(matcher.group(1));
        }

        return features;
    }

    /**
     * Internal class to hold query components.
     */
    private static class QueryComponents {
        String brand;
        String productType;
        String modelNumber;
        String baseModel;
    }
}
