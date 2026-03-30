package com.gary.assistant.util;

/**
 * Mock HTML responses for testing scrapers.
 */
public class MockHtmlFixtures {

    // Amazon Fixtures

    public static String amazonSearchPageWithResults() {
        return """
            <html>
            <body>
                <div data-component-type="s-search-result" data-asin="B09HM94VDS">
                    <h2>
                        <a href="/Logitech-MX-Master-3S/dp/B09HM94VDS">
                            <span>Mouse Logitech MX Master 3S Sem Fio</span>
                        </a>
                    </h2>
                    <span class="a-price-whole">449,90</span>
                    <span class="a-icon-alt">4,7 de 5 estrelas</span>
                    <span aria-label="8.234 avaliações">8.234</span>
                    <img class="s-image" src="https://m.media-amazon.com/images/I/test1.jpg"/>
                </div>
                <div data-component-type="s-search-result" data-asin="B09HM94VDS2">
                    <h2>
                        <a href="/Another-Product/dp/B09HM94VDS2">
                            <span>Another Test Product</span>
                        </a>
                    </h2>
                    <span class="a-price-whole">299,90</span>
                    <span class="a-icon-alt">4,5 de 5 estrelas</span>
                    <span aria-label="1.500 avaliações">1.500</span>
                    <img class="s-image" src="https://m.media-amazon.com/images/I/test2.jpg"/>
                </div>
            </body>
            </html>
            """;
    }

    public static String amazonSearchPageEmpty() {
        return """
            <html>
            <body>
                <div class="s-no-results">No results found</div>
            </body>
            </html>
            """;
    }

    public static String amazonProductDetailsPage() {
        return """
            <html>
            <body>
                <span id="productTitle">Mouse Logitech MX Master 3S Sem Fio Recarregável</span>
                <span class="a-price-whole">449,90</span>
                <span class="a-icon-alt">4,7 de 5 estrelas</span>
                <span aria-label="8.234 avaliações">8.234</span>
                <div id="feature-bullets">
                    <ul>
                        <li>Sensor de alta precisão</li>
                        <li>Bateria de até 70 dias</li>
                        <li>Conexão Bluetooth</li>
                    </ul>
                </div>
                <img id="landingImage" src="https://m.media-amazon.com/images/I/product-main.jpg"/>
            </body>
            </html>
            """;
    }

    public static String amazonProductWithoutPrice() {
        return """
            <html>
            <body>
                <div data-component-type="s-search-result" data-asin="TEST123">
                    <h2>
                        <a href="/Product/dp/TEST123">
                            <span>Product Without Price</span>
                        </a>
                    </h2>
                    <span class="a-icon-alt">4,5 de 5 estrelas</span>
                </div>
            </body>
            </html>
            """;
    }

    public static String amazonProductWithoutRating() {
        return """
            <html>
            <body>
                <div data-component-type="s-search-result" data-asin="TEST456">
                    <h2>
                        <a href="/Product/dp/TEST456">
                            <span>Product Without Rating</span>
                        </a>
                    </h2>
                    <span class="a-price-whole">199,90</span>
                </div>
            </body>
            </html>
            """;
    }

    public static String amazonProductWithMissingElements() {
        return """
            <html>
            <body>
                <div data-component-type="s-search-result" data-asin="">
                    <span>Incomplete Product</span>
                </div>
            </body>
            </html>
            """;
    }

    // Mercado Livre Fixtures

    public static String mercadoLivreSearchPageWithResults() {
        return """
            <html>
            <body>
                <li class="ui-search-layout__item">
                    <a class="ui-search-link" href="/p/MLB-123456789">
                        <h2 class="ui-search-item__title">Mouse Logitech MX Master 3S Wireless</h2>
                    </a>
                    <span class="andes-money-amount__fraction">429</span>
                    <span class="ui-search-reviews__rating-number">4.8</span>
                    <span class="ui-search-reviews__amount">(234)</span>
                    <span class="ui-search-item__shipping-label">Frete grátis</span>
                    <img class="ui-search-result-image__element" src="https://http2.mlstatic.com/test1.jpg"/>
                </li>
                <li class="ui-search-layout__item">
                    <a class="ui-search-link" href="/p/MLB-987654321">
                        <h2 class="ui-search-item__title">Another Product</h2>
                    </a>
                    <span class="andes-money-amount__fraction">299</span>
                    <span class="ui-search-reviews__rating-number">4.5</span>
                    <span class="ui-search-reviews__amount">(100)</span>
                    <img class="ui-search-result-image__element" data-src="https://http2.mlstatic.com/test2.jpg"/>
                </li>
            </body>
            </html>
            """;
    }

    public static String mercadoLivreSearchPageEmpty() {
        return """
            <html>
            <body>
                <div class="ui-search-rescue">
                    <p>Não encontramos produtos</p>
                </div>
            </body>
            </html>
            """;
    }

    public static String mercadoLivreProductDetailsPage() {
        return """
            <html>
            <body>
                <h1 class="ui-pdp-title">Mouse Logitech MX Master 3S Sem Fio Recarregável</h1>
                <span class="andes-money-amount__fraction">429</span>
                <span class="ui-pdp-review__rating">4.8</span>
                <span class="ui-pdp-review__amount">(234)</span>
                <div class="ui-pdp-description__content">
                    <p>Mouse ergonômico de alta precisão</p>
                    <p>Bateria recarregável de longa duração</p>
                </div>
                <img class="ui-pdp-image" src="https://http2.mlstatic.com/product-main.jpg"/>
            </body>
            </html>
            """;
    }

    public static String mercadoLivreProductWithoutPrice() {
        return """
            <html>
            <body>
                <li class="ui-search-layout__item">
                    <a class="ui-search-link" href="/p/MLB-111111111">
                        <h2 class="ui-search-item__title">Product Without Price</h2>
                    </a>
                    <span class="ui-search-reviews__rating-number">4.5</span>
                </li>
            </body>
            </html>
            """;
    }

    public static String mercadoLivreProductWithoutRating() {
        return """
            <html>
            <body>
                <li class="ui-search-layout__item">
                    <a class="ui-search-link" href="/p/MLB-222222222">
                        <h2 class="ui-search-item__title">Product Without Rating</h2>
                    </a>
                    <span class="andes-money-amount__fraction">199</span>
                </li>
            </body>
            </html>
            """;
    }

    public static String mercadoLivreProductWithMissingElements() {
        return """
            <html>
            <body>
                <li class="ui-search-layout__item">
                    <span>Incomplete Product</span>
                </li>
            </body>
            </html>
            """;
    }

    public static String mercadoLivreProductWithInvalidUrl() {
        return """
            <html>
            <body>
                <li class="ui-search-layout__item">
                    <a class="ui-search-link" href="/invalid-url-format">
                        <h2 class="ui-search-item__title">Product With Invalid URL</h2>
                    </a>
                </li>
            </body>
            </html>
            """;
    }
}
