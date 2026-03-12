package com.gary.assistant.exception;

import com.gary.assistant.model.Platform;

public class ScraperException extends RuntimeException {
    private final Platform platform;

    public ScraperException(Platform platform, String message) {
        super("Scraper error for " + platform.getDisplayName() + ": " + message);
        this.platform = platform;
    }

    public ScraperException(Platform platform, String message, Throwable cause) {
        super("Scraper error for " + platform.getDisplayName() + ": " + message, cause);
        this.platform = platform;
    }

    public Platform getPlatform() {
        return platform;
    }
}
