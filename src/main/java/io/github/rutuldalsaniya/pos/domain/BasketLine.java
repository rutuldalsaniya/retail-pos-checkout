package io.github.rutuldalsaniya.pos.domain;

import java.math.BigDecimal;

public record BasketLine(Product product, int quantity) {

    public BasketLine {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1");
        }
    }

    public BigDecimal lineTotal() {
        return Money.round(product.unitPrice().multiply(BigDecimal.valueOf(quantity)));
    }

    BasketLine withQuantity(int newQuantity) {
        return new BasketLine(product, newQuantity);
    }
}
