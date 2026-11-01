package io.github.rutuldalsaniya.pos.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Stand-in for a real payment provider: approves card payments up to a
 * configurable limit and declines anything above it.
 */
@Component
public class SimulatedCardGateway implements PaymentGateway {

    private final BigDecimal limit;

    public SimulatedCardGateway(@Value("${pos.payment.card-limit:500.00}") BigDecimal limit) {
        this.limit = limit;
    }

    @Override
    public PaymentResult authorize(PaymentRequest request) {
        if (request.amount().compareTo(limit) > 0) {
            return PaymentResult.declined("Card limit exceeded");
        }
        return PaymentResult.approved("CARD-" + UUID.randomUUID());
    }
}
