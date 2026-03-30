package com.gary.assistant.util;

import com.gary.assistant.model.Currency;
import com.gary.assistant.model.Platform;
import com.gary.assistant.model.Price;
import com.gary.assistant.model.Product;
import com.gary.assistant.model.Rating;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Test data builder utility for creating test objects.
 */
public class TestDataBuilder {

    public static Product createAmazonProduct() {
        return createAmazonProduct("B09HM94VDS", "Logitech MX Master 3S");
    }

    public static Product createAmazonProduct(String asin, String name) {
        Product product = new Product(
            name,
            Platform.AMAZON,
            asin,
            "https://www.amazon.com.br/dp/" + asin
        );
        product.updatePrice(new Price(
            new BigDecimal("449.90"),
            Currency.BRL,
            BigDecimal.ZERO
        ));
        product.updateRating(new Rating(4.7, 8234));
        product.setImageUrl("https://m.media-amazon.com/images/I/test.jpg");
        return product;
    }

    public static Product createMercadoLivreProduct() {
        return createMercadoLivreProduct("MLB-123456789", "Mouse Logitech MX Master 3S");
    }

    public static Product createMercadoLivreProduct(String productId, String name) {
        Product product = new Product(
            name,
            Platform.MERCADO_LIVRE,
            productId,
            "https://www.mercadolivre.com.br/p/" + productId
        );
        product.updatePrice(new Price(
            new BigDecimal("429.90"),
            Currency.BRL,
            BigDecimal.ZERO
        ));
        product.updateRating(new Rating(4.8, 234));
        product.setImageUrl("https://http2.mlstatic.com/test.jpg");
        return product;
    }

    public static List<Product> createProductList(int count, Platform platform) {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (platform == Platform.AMAZON) {
                products.add(createAmazonProduct("TEST" + i, "Test Product " + i));
            } else {
                products.add(createMercadoLivreProduct("MLB-" + i, "Test Product " + i));
            }
        }
        return products;
    }
}
