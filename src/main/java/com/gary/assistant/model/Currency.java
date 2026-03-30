package com.gary.assistant.model;

public enum Currency {
    USD("USD", "$", "US Dollar"),
    BRL("BRL", "R$", "Brazilian Real");

    private final String code;
    private final String symbol;
    private final String displayName;

    Currency(String code, String symbol, String displayName) {
        this.code = code;
        this.symbol = symbol;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDisplayName() {
        return displayName;
    }
}
