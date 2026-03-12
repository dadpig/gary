package com.gary.assistant.model;

public enum Platform {
    AMAZON("Amazon", "amazon.com.br"),
    MERCADO_LIVRE("Mercado Livre", "mercadolivre.com.br");

    private final String displayName;
    private final String domain;

    Platform(String displayName, String domain) {
        this.displayName = displayName;
        this.domain = domain;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDomain() {
        return domain;
    }
}
