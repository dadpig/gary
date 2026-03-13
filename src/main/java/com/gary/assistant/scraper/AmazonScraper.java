package com.gary.assistant.scraper;

import com.gary.assistant.exception.ScraperException;
import com.gary.assistant.model.Currency;
import com.gary.assistant.model.Platform;
import com.gary.assistant.model.Price;
import com.gary.assistant.model.Product;
import com.gary.assistant.model.Rating;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AmazonScraper implements ProductScraper {

    private static final Logger logger = LoggerFactory.getLogger(AmazonScraper.class);
    private static final String BASE_URL = "https://www.amazon.com.br";
    private static final String SEARCH_URL = BASE_URL + "/s?k=%s";
    private static final Pattern PRICE_PATTERN = Pattern.compile("R\\$\\s*([\\d.,]+)");
    private static final Pattern RATING_PATTERN = Pattern.compile("([\\d,]+)\\s+de\\s+5");
    private static final Pattern REVIEW_COUNT_PATTERN = Pattern.compile("([\\d.]+)");

    private final CloseableHttpClient httpClient;

    public AmazonScraper(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Platform getPlatform() {
        return Platform.AMAZON;
    }

    @Override
    @RateLimiter(name = "amazonScraper", fallbackMethod = "searchFallback")
    @CircuitBreaker(name = "scraperCircuitBreaker", fallbackMethod = "searchFallback")
    public List<Product> search(String query, int maxResults) {
        logger.info("Searching Amazon for: {}", query);

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String searchUrl = String.format(SEARCH_URL, encodedQuery);

            String html = fetchPage(searchUrl);
            Document doc = Jsoup.parse(html);

            List<Product> products = new ArrayList<>();
            Elements productElements = doc.select("div[data-component-type='s-search-result']");

            for (Element element : productElements) {
                if (products.size() >= maxResults) {
                    break;
                }

                try {
                    Product product = parseSearchResult(element);
                    if (product != null) {
                        products.add(product);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse product element: {}", e.getMessage());
                }
            }

            logger.info("Found {} products for query: {}", products.size(), query);
            return products;

        } catch (IOException e) {
            logger.error("Failed to search Amazon: {}", e.getMessage());
            throw new ScraperException(Platform.AMAZON, "Failed to fetch search results: " + e.getMessage());
        }
    }

    private List<Product> searchFallback(String query, int maxResults, Exception e) {
        logger.warn("Amazon search fallback triggered for query '{}': {}", query, e.getMessage());
        return List.of();
    }

    @Override
    @RateLimiter(name = "amazonScraper", fallbackMethod = "getProductDetailsFallback")
    @CircuitBreaker(name = "scraperCircuitBreaker", fallbackMethod = "getProductDetailsFallback")
    public Product getProductDetails(String productId) {
        logger.info("Fetching Amazon product details for ASIN: {}", productId);

        try {
            String productUrl = BASE_URL + "/dp/" + productId;
            String html = fetchPage(productUrl);
            Document doc = Jsoup.parse(html);

            return parseProductDetails(doc, productId, productUrl);

        } catch (IOException e) {
            logger.error("Failed to fetch product details: {}", e.getMessage());
            throw new ScraperException(Platform.AMAZON, "Failed to fetch product details: " + e.getMessage());
        }
    }

    private Product getProductDetailsFallback(String productId, Exception e) {
        logger.warn("Amazon product details fallback triggered for ID '{}': {}", productId, e.getMessage());
        throw new ScraperException(Platform.AMAZON, "Service temporarily unavailable");
    }

    @Override
    public boolean isAvailable() {
        try {
            HttpGet request = new HttpGet(BASE_URL);
            return httpClient.execute(request, response -> response.getCode() == 200);
        } catch (IOException e) {
            logger.error("Health check failed: {}", e.getMessage());
            return false;
        }
    }

    private String fetchPage(String url) throws IOException {
        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        request.setHeader("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7");
        request.setHeader("Accept-Encoding", "gzip, deflate, br");
        request.setHeader("Connection", "keep-alive");

        return httpClient.execute(request, response -> {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                throw new IOException("HTTP error code: " + statusCode);
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IOException("Empty response");
            }

            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        });
    }

    private Product parseSearchResult(Element element) {
        // Extract ASIN
        String asin = element.attr("data-asin");
        if (asin == null || asin.isEmpty()) {
            return null;
        }

        // Extract title
        Element titleElement = element.selectFirst("h2 a span");
        if (titleElement == null) {
            return null;
        }
        String title = titleElement.text();

        // Extract URL
        Element linkElement = element.selectFirst("h2 a");
        String relativeUrl = linkElement != null ? linkElement.attr("href") : "";
        String url = relativeUrl.startsWith("http") ? relativeUrl : BASE_URL + relativeUrl;

        // Create product
        Product product = new Product(title, Platform.AMAZON, asin, url);

        // Extract price
        Element priceElement = element.selectFirst("span.a-price-whole");
        if (priceElement != null) {
            String priceText = priceElement.text();
            BigDecimal price = parsePrice(priceText);
            if (price != null) {
                product.updatePrice(new Price(price, Currency.BRL, BigDecimal.ZERO));
            }
        }

        // Extract rating
        Element ratingElement = element.selectFirst("span.a-icon-alt");
        if (ratingElement != null) {
            String ratingText = ratingElement.text();
            Rating rating = parseRating(ratingText, element);
            if (rating != null) {
                product.updateRating(rating);
            }
        }

        // Extract image
        Element imageElement = element.selectFirst("img.s-image");
        if (imageElement != null) {
            String imageUrl = imageElement.attr("src");
            product.setImageUrl(imageUrl);
        }

        return product;
    }

    private Product parseProductDetails(Document doc, String asin, String url) {
        // Extract title
        Element titleElement = doc.selectFirst("#productTitle");
        if (titleElement == null) {
            throw new ScraperException(Platform.AMAZON, "Product title not found");
        }
        String title = titleElement.text().trim();

        Product product = new Product(title, Platform.AMAZON, asin, url);

        // Extract price
        Element priceElement = doc.selectFirst("span.a-price-whole");
        if (priceElement != null) {
            String priceText = priceElement.text();
            BigDecimal price = parsePrice(priceText);
            if (price != null) {
                product.updatePrice(new Price(price, Currency.BRL, BigDecimal.ZERO));
            }
        }

        // Extract description
        Element descElement = doc.selectFirst("#feature-bullets");
        if (descElement != null) {
            product.setDescription(descElement.text().trim());
        }

        // Extract rating
        Element ratingElement = doc.selectFirst("span.a-icon-alt");
        if (ratingElement != null) {
            String ratingText = ratingElement.text();
            Rating rating = parseRating(ratingText, doc);
            if (rating != null) {
                product.updateRating(rating);
            }
        }

        // Extract image
        Element imageElement = doc.selectFirst("#landingImage");
        if (imageElement != null) {
            String imageUrl = imageElement.attr("src");
            product.setImageUrl(imageUrl);
        }

        return product;
    }

    private BigDecimal parsePrice(String priceText) {
        if (priceText == null || priceText.isEmpty()) {
            return null;
        }

        try {
            // Remove currency symbols and clean up
            String cleaned = priceText.replaceAll("[^\\d.,]", "");
            // Replace comma with dot for decimal separator
            cleaned = cleaned.replace(".", "").replace(",", ".");
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse price: {}", priceText);
            return null;
        }
    }

    private Rating parseRating(String ratingText, Element context) {
        if (ratingText == null || ratingText.isEmpty()) {
            return null;
        }

        try {
            Matcher matcher = RATING_PATTERN.matcher(ratingText);
            Double score = null;
            if (matcher.find()) {
                String scoreStr = matcher.group(1).replace(",", ".");
                score = Double.parseDouble(scoreStr);
            }

            Integer reviewCount = null;
            Element reviewElement = context.selectFirst("span[aria-label*='avaliações']");
            if (reviewElement != null) {
                String reviewText = reviewElement.attr("aria-label");
                Matcher reviewMatcher = REVIEW_COUNT_PATTERN.matcher(reviewText);
                if (reviewMatcher.find()) {
                    String countStr = reviewMatcher.group(1).replace(".", "");
                    reviewCount = Integer.parseInt(countStr);
                }
            }

            if (score != null) {
                return new Rating(score, reviewCount);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse rating: {}", ratingText);
        }

        return null;
    }
}
