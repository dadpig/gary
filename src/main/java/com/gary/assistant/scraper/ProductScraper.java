package com.gary.assistant.scraper;

import com.gary.assistant.model.Platform;
import com.gary.assistant.model.Product;

import java.util.List;

public interface ProductScraper {

    Platform getPlatform();

    List<Product> search(String query, int maxResults);

    Product getProductDetails(String productId);

    boolean isAvailable();
}
