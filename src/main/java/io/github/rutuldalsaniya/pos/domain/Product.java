package io.github.rutuldalsaniya.pos.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A sellable article. {@code unitPrice} is the gross price in EUR.
 */
public record Product(String sku, String name, BigDecimal unitPrice, VatRate vatRate, String category) {

    public Product {
        Objects.requireNonNull(sku, "sku");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(unitPrice, "unitPrice");
        Objects.requireNonNull(vatRate, "vatRate");
        Objects.requireNonNull(category, "category");
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice must not be negative");
        }
    }
}
