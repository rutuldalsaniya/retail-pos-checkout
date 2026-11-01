package io.github.rutuldalsaniya.pos.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record PricedBasket(
        List<BasketLine> lines,
        BigDecimal subtotal,
        List<AppliedDiscount> discounts,
        BigDecimal total,
        Map<VatRate, BigDecimal> vatBreakdown) {
}
