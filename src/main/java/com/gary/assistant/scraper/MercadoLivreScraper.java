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
public class MercadoLivreScraper implements ProductScraper {

    private static final Logger logger = LoggerFactory.getLogger(MercadoLivreScraper.class);
    private static final String BASE_URL = "https://www.mercadolivre.com.br";
    private static final String SEARCH_URL = BASE_URL + "/jm/search?as_word=%s";
    private static final Pattern PRICE_PATTERN = Pattern.compile("R\\$\\s*([\\d.,]+)");
    private static final Pattern REVIEW_COUNT_PATTERN = Pattern.compile("\\((\\d+)\\)");

    private final CloseableHttpClient httpClient;

    public MercadoLivreScraper(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Platform getPlatform() {
        return Platform.MERCADO_LIVRE;
    }

    @Override
    @RateLimiter(name = "mercadoLivreScraper", fallbackMethod = "searchFallback")
    @CircuitBreaker(name = "scraperCircuitBreaker", fallbackMethod = "searchFallback")
    public List<Product> search(String query, int maxResults) {
        logger.info("Searching Mercado Livre for: {}", query);

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String searchUrl = String.format(SEARCH_URL, encodedQuery);

            String html = fetchPage(searchUrl);
            Document doc = Jsoup.parse(html);

            List<Product> products = new ArrayList<>();
            Elements productElements = doc.select("li.ui-search-layout__item");

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
            logger.error("Failed to search Mercado Livre: {}", e.getMessage());
            throw new ScraperException(Platform.MERCADO_LIVRE, "Failed to fetch search results: " + e.getMessage());
        }
    }

    private List<Product> searchFallback(String query, int maxResults, Exception e) {
        logger.warn("Mercado Livre search fallback triggered for query '{}': {}", query, e.getMessage());
        return List.of();
    }

    @Override
    @RateLimiter(name = "mercadoLivreScraper", fallbackMethod = "getProductDetailsFallback")
    @CircuitBreaker(name = "scraperCircuitBreaker", fallbackMethod = "getProductDetailsFallback")
    public Product getProductDetails(String productId) {
        logger.info("Fetching Mercado Livre product details for: {}", productId);

        try {
            String productUrl = BASE_URL + "/p/" + productId;
            String html = fetchPage(productUrl);
            Document doc = Jsoup.parse(html);

            return parseProductDetails(doc, productId, productUrl);

        } catch (IOException e) {
            logger.error("Failed to fetch product details: {}", e.getMessage());
            throw new ScraperException(Platform.MERCADO_LIVRE, "Failed to fetch product details: " + e.getMessage());
        }
    }

    private Product getProductDetailsFallback(String productId, Exception e) {
        logger.warn("Mercado Livre product details fallback triggered for ID '{}': {}", productId, e.getMessage());
        throw new ScraperException(Platform.MERCADO_LIVRE, "Service temporarily unavailable");
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
        // Extract product ID from link
        Element linkElement = element.selectFirst("a.ui-search-link");
        if (linkElement == null) {
            return null;
        }

        String url = linkElement.attr("href");
        String productId = extractProductId(url);
        if (productId == null) {
            return null;
        }

        // Extract title
        Element titleElement = element.selectFirst("h2.ui-search-item__title");
        if (titleElement == null) {
            return null;
        }
        String title = titleElement.text();

        // Create product
        Product product = new Product(title, Platform.MERCADO_LIVRE, productId, url);

        // Extract price
        Element priceElement = element.selectFirst("span.andes-money-amount__fraction");
        if (priceElement != null) {
            String priceText = priceElement.text();
            BigDecimal price = parsePrice(priceText);
            if (price != null) {
                // Check for shipping
                Element shippingElement = element.selectFirst("span.ui-search-item__shipping-label");
                BigDecimal shipping = shippingElement != null &&
                                     shippingElement.text().toLowerCase().contains("grátis")
                                     ? BigDecimal.ZERO : null;

                product.updatePrice(new Price(price, Currency.BRL, shipping != null ? shipping : BigDecimal.ZERO));
            }
        }

        // Extract rating
        Element ratingElement = element.selectFirst("span.ui-search-reviews__rating-number");
        if (ratingElement != null) {
            String ratingText = ratingElement.text();
            Element reviewElement = element.selectFirst("span.ui-search-reviews__amount");
            String reviewText = reviewElement != null ? reviewElement.text() : null;
            Rating rating = parseRating(ratingText, reviewText);
            if (rating != null) {
                product.updateRating(rating);
            }
        }

        // Extract image
        Element imageElement = element.selectFirst("img.ui-search-result-image__element");
        if (imageElement != null) {
            String imageUrl = imageElement.attr("src");
            if (imageUrl.isEmpty()) {
                imageUrl = imageElement.attr("data-src");
            }
            product.setImageUrl(imageUrl);
        }

        return product;
    }

    private Product parseProductDetails(Document doc, String productId, String url) {
        // Extract title
        Element titleElement = doc.selectFirst("h1.ui-pdp-title");
        if (titleElement == null) {
            throw new ScraperException(Platform.MERCADO_LIVRE, "Product title not found");
        }
        String title = titleElement.text().trim();

        Product product = new Product(title, Platform.MERCADO_LIVRE, productId, url);

        // Extract price
        Element priceElement = doc.selectFirst("span.andes-money-amount__fraction");
        if (priceElement != null) {
            String priceText = priceElement.text();
            BigDecimal price = parsePrice(priceText);
            if (price != null) {
                product.updatePrice(new Price(price, Currency.BRL, BigDecimal.ZERO));
            }
        }

        // Extract description
        Element descElement = doc.selectFirst("div.ui-pdp-description__content");
        if (descElement != null) {
            product.setDescription(descElement.text().trim());
        }

        // Extract rating
        Element ratingElement = doc.selectFirst("span.ui-pdp-review__rating");
        if (ratingElement != null) {
            String ratingText = ratingElement.text();
            Element reviewElement = doc.selectFirst("span.ui-pdp-review__amount");
            String reviewText = reviewElement != null ? reviewElement.text() : null;
            Rating rating = parseRating(ratingText, reviewText);
            if (rating != null) {
                product.updateRating(rating);
            }
        }

        // Extract image
        Element imageElement = doc.selectFirst("img.ui-pdp-image");
        if (imageElement != null) {
            String imageUrl = imageElement.attr("src");
            product.setImageUrl(imageUrl);
        }

        return product;
    }

    private String extractProductId(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // Extract ID from URL pattern like: /MLB-123456789-product-name/p
        String[] parts = url.split("/");
        for (String part : parts) {
            if (part.startsWith("MLB-") || part.startsWith("MLA-")) {
                return part;
            }
        }

        return null;
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

    private Rating parseRating(String ratingText, String reviewText) {
        if (ratingText == null || ratingText.isEmpty()) {
            return null;
        }

        try {
            Double score = Double.parseDouble(ratingText.trim());

            Integer reviewCount = null;
            if (reviewText != null) {
                Matcher matcher = REVIEW_COUNT_PATTERN.matcher(reviewText);
                if (matcher.find()) {
                    reviewCount = Integer.parseInt(matcher.group(1));
                }
            }

            return new Rating(score, reviewCount);
        } catch (Exception e) {
            logger.warn("Failed to parse rating: {} / {}", ratingText, reviewText);
            return null;
        }
    }
}
