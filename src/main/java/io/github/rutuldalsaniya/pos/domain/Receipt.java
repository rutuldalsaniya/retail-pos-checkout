package io.github.rutuldalsaniya.pos.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Receipt(
        String receiptNumber,
        String basketId,
        Instant timestamp,
        PricedBasket pricing,
        PaymentMethod paymentMethod,
        String transactionId,
        BigDecimal change) {
}
