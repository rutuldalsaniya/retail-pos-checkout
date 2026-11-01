package io.github.rutuldalsaniya.pos.payment;

import java.math.BigDecimal;

public record PaymentRequest(String basketId, BigDecimal amount, String currency) {
}
