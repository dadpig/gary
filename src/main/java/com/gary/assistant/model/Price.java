package com.gary.assistant.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Embeddable
public class Price {
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    private BigDecimal shippingCost;
    private Instant lastUpdated;

    protected Price() {
    }

    public Price(BigDecimal amount, Currency currency, BigDecimal shippingCost) {
        this.amount = amount;
        this.currency = currency;
        this.shippingCost = shippingCost;
        this.lastUpdated = Instant.now();
    }

    public BigDecimal getTotal() {
        return amount.add(shippingCost != null ? shippingCost : BigDecimal.ZERO);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return Objects.equals(amount, price.amount) &&
               currency == price.currency &&
               Objects.equals(shippingCost, price.shippingCost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency, shippingCost);
    }

    @Override
    public String toString() {
        return currency.getSymbol() + amount +
               (shippingCost != null && shippingCost.compareTo(BigDecimal.ZERO) > 0
                   ? " (+" + currency.getSymbol() + shippingCost + " shipping)"
                   : "");
    }
}
