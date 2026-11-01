package io.github.rutuldalsaniya.pos.web;

import io.github.rutuldalsaniya.pos.domain.AppliedDiscount;
import io.github.rutuldalsaniya.pos.domain.Basket;
import io.github.rutuldalsaniya.pos.domain.BasketLine;
import io.github.rutuldalsaniya.pos.domain.PaymentMethod;
import io.github.rutuldalsaniya.pos.domain.PricedBasket;
import io.github.rutuldalsaniya.pos.domain.Receipt;
import io.github.rutuldalsaniya.pos.domain.VatRate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Request and response bodies of the REST API. */
public final class BasketDtos {

    private BasketDtos() {
    }

    public record AddItemRequest(@NotBlank String sku, @Min(1) int quantity) {
    }

    public record CheckoutRequest(@NotNull PaymentMethod paymentMethod,
                                  @DecimalMin("0.00") BigDecimal cashTendered) {
    }

    public record LineResponse(String sku, String name, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {

        static LineResponse from(BasketLine line) {
            return new LineResponse(line.product().sku(), line.product().name(), line.quantity(),
                    line.product().unitPrice(), line.lineTotal());
        }
    }

    public record DiscountResponse(String name, BigDecimal amount) {

        static DiscountResponse from(AppliedDiscount discount) {
            return new DiscountResponse(discount.name(), discount.amount());
        }
    }

    public record BasketResponse(String id, String status, List<LineResponse> lines, BigDecimal subtotal,
                                 List<DiscountResponse> discounts, BigDecimal total, Map<String, BigDecimal> vat) {

        public static BasketResponse from(Basket basket, PricedBasket pricing) {
            return new BasketResponse(basket.id(), basket.status().name(),
                    pricing.lines().stream().map(LineResponse::from).toList(),
                    pricing.subtotal(),
                    pricing.discounts().stream().map(DiscountResponse::from).toList(),
                    pricing.total(),
                    vatLabels(pricing.vatBreakdown()));
        }
    }

    public record ReceiptResponse(String receiptNumber, String basketId, Instant timestamp,
                                  List<LineResponse> lines, BigDecimal subtotal, List<DiscountResponse> discounts,
                                  BigDecimal total, Map<String, BigDecimal> vat, String paymentMethod,
                                  String transactionId, BigDecimal change) {

        public static ReceiptResponse from(Receipt receipt) {
            PricedBasket p = receipt.pricing();
            return new ReceiptResponse(receipt.receiptNumber(), receipt.basketId(), receipt.timestamp(),
                    p.lines().stream().map(LineResponse::from).toList(),
                    p.subtotal(),
                    p.discounts().stream().map(DiscountResponse::from).toList(),
                    p.total(),
                    vatLabels(p.vatBreakdown()),
                    receipt.paymentMethod().name(),
                    receipt.transactionId(),
                    receipt.change());
        }
    }

    private static Map<String, BigDecimal> vatLabels(Map<VatRate, BigDecimal> breakdown) {
        Map<String, BigDecimal> labelled = new LinkedHashMap<>();
        breakdown.forEach((rate, amount) -> labelled.put(rate.percent().toPlainString() + "%", amount));
        return labelled;
    }
}
