package com.gary.assistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for fallback search strategies.
 */
@Configuration
@ConfigurationProperties(prefix = "gary.fallback")
public class FallbackConfig {

    /**
     * Enable fallback search when primary search returns insufficient results.
     */
    private boolean enabled = true;

    /**
     * Minimum number of results before triggering fallback.
     */
    private int minResultsThreshold = 3;

    /**
     * Maximum number of fallback attempts.
     */
    private int maxAttempts = 3;

    /**
     * Minimum similarity score (0.0 to 1.0) for considering products as similar.
     */
    private double similarityThreshold = 0.3;

    /**
     * Maximum total results to return (including fallback results).
     */
    private int maxTotalResults = 20;

    /**
     * Enable cross-platform fallback (search other platforms if one fails).
     */
    private boolean crossPlatformEnabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMinResultsThreshold() {
        return minResultsThreshold;
    }

    public void setMinResultsThreshold(int minResultsThreshold) {
        this.minResultsThreshold = minResultsThreshold;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public int getMaxTotalResults() {
        return maxTotalResults;
    }

    public void setMaxTotalResults(int maxTotalResults) {
        this.maxTotalResults = maxTotalResults;
    }

    public boolean isCrossPlatformEnabled() {
        return crossPlatformEnabled;
    }

    public void setCrossPlatformEnabled(boolean crossPlatformEnabled) {
        this.crossPlatformEnabled = crossPlatformEnabled;
    }
}
