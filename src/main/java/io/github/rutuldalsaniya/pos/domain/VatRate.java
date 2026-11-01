package io.github.rutuldalsaniya.pos.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * German VAT rates. Retail prices are gross prices (VAT included).
 */
public enum VatRate {
    REDUCED(new BigDecimal("7")),
    STANDARD(new BigDecimal("19"));

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final BigDecimal percent;

    VatRate(BigDecimal percent) {
        this.percent = percent;
    }

    public BigDecimal percent() {
        return percent;
    }

    /** VAT contained in a gross amount: gross * rate / (100 + rate). */
    public BigDecimal vatIncludedIn(BigDecimal gross) {
        return gross.multiply(percent)
                .divide(HUNDRED.add(percent), 2, RoundingMode.HALF_UP);
    }
}
