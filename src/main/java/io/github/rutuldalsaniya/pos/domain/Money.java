package io.github.rutuldalsaniya.pos.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Small helper for EUR amounts (2 decimal places, commercial rounding). */
public final class Money {

    public static final BigDecimal ZERO = new BigDecimal("0.00");

    private Money() {
    }

    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal of(String amount) {
        return round(new BigDecimal(amount));
    }
}
