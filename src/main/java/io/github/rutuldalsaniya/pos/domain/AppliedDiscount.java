package io.github.rutuldalsaniya.pos.domain;

import java.math.BigDecimal;

/**
 * A discount granted by a promotion. The VAT rate is kept so the
 * receipt can show a correct VAT breakdown after discounts.
 */
public record AppliedDiscount(String name, BigDecimal amount, VatRate vatRate) {
}
