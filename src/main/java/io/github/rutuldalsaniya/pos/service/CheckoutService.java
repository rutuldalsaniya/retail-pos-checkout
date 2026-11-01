package io.github.rutuldalsaniya.pos.service;

import io.github.rutuldalsaniya.pos.domain.Basket;
import io.github.rutuldalsaniya.pos.domain.BasketStatus;
import io.github.rutuldalsaniya.pos.domain.Money;
import io.github.rutuldalsaniya.pos.domain.PaymentMethod;
import io.github.rutuldalsaniya.pos.domain.PricedBasket;
import io.github.rutuldalsaniya.pos.domain.Receipt;
import io.github.rutuldalsaniya.pos.payment.PaymentGateway;
import io.github.rutuldalsaniya.pos.payment.PaymentRequest;
import io.github.rutuldalsaniya.pos.payment.PaymentResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Completes a sale: prices the basket, takes payment and issues a receipt.
 * The basket is only marked as paid after the payment succeeded.
 */
@Service
public class CheckoutService {

    private final BasketService basketService;
    private final PricingService pricingService;
    private final PaymentGateway paymentGateway;
    private final Clock clock;

    public CheckoutService(BasketService basketService,
                           PricingService pricingService,
                           PaymentGateway paymentGateway,
                           Clock clock) {
        this.basketService = basketService;
        this.pricingService = pricingService;
        this.paymentGateway = paymentGateway;
        this.clock = clock;
    }

    public Receipt checkout(String basketId, PaymentMethod method, BigDecimal cashTendered) {
        Basket basket = basketService.get(basketId);
        if (basket.status() != BasketStatus.OPEN) {
            throw new CheckoutException("Basket " + basketId + " is already paid");
        }
        if (basket.isEmpty()) {
            throw new CheckoutException("Basket " + basketId + " is empty");
        }

        PricedBasket pricing = pricingService.price(basket);
        BigDecimal total = pricing.total();

        String transactionId;
        BigDecimal change = Money.ZERO;

        switch (method) {
            case CASH -> {
                if (cashTendered == null || cashTendered.compareTo(total) < 0) {
                    throw new PaymentDeclinedException("Cash tendered is less than the total of " + total + " EUR");
                }
                change = Money.round(cashTendered.subtract(total));
                transactionId = "CASH-" + UUID.randomUUID();
            }
            case CARD -> {
                PaymentResult result = paymentGateway.authorize(new PaymentRequest(basketId, total, "EUR"));
                if (!result.approved()) {
                    throw new PaymentDeclinedException(result.message());
                }
                transactionId = result.transactionId();
            }
            default -> throw new IllegalArgumentException("Unsupported payment method " + method);
        }

        basket.markPaid();
        return new Receipt(newReceiptNumber(), basketId, Instant.now(clock), pricing, method, transactionId, change);
    }

    private String newReceiptNumber() {
        return "R-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
