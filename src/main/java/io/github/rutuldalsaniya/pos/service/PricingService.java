package io.github.rutuldalsaniya.pos.service;

import io.github.rutuldalsaniya.pos.domain.AppliedDiscount;
import io.github.rutuldalsaniya.pos.domain.Basket;
import io.github.rutuldalsaniya.pos.domain.BasketLine;
import io.github.rutuldalsaniya.pos.domain.Money;
import io.github.rutuldalsaniya.pos.domain.PricedBasket;
import io.github.rutuldalsaniya.pos.domain.VatRate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates subtotal, promotions, total and the VAT breakdown of a basket.
 */
@Service
public class PricingService {

    private final List<Promotion> promotions;

    public PricingService(List<Promotion> promotions) {
        this.promotions = List.copyOf(promotions);
    }

    public PricedBasket price(Basket basket) {
        List<BasketLine> lines = basket.lines();

        BigDecimal subtotal = lines.stream()
                .map(BasketLine::lineTotal)
                .reduce(Money.ZERO, BigDecimal::add);

        List<AppliedDiscount> discounts = promotions.stream()
                .flatMap(p -> p.apply(basket).stream())
                .toList();

        BigDecimal discountTotal = discounts.stream()
                .map(AppliedDiscount::amount)
                .reduce(Money.ZERO, BigDecimal::add);

        BigDecimal total = subtotal.subtract(discountTotal).max(Money.ZERO);

        return new PricedBasket(lines, subtotal, discounts, total, vatBreakdown(lines, discounts));
    }

    private Map<VatRate, BigDecimal> vatBreakdown(List<BasketLine> lines, List<AppliedDiscount> discounts) {
        Map<VatRate, BigDecimal> grossByRate = new EnumMap<>(VatRate.class);
        lines.forEach(l -> grossByRate.merge(l.product().vatRate(), l.lineTotal(), BigDecimal::add));
        discounts.forEach(d -> grossByRate.merge(d.vatRate(), d.amount().negate(), BigDecimal::add));

        Map<VatRate, BigDecimal> vat = new EnumMap<>(VatRate.class);
        grossByRate.forEach((rate, gross) -> vat.put(rate, rate.vatIncludedIn(gross.max(Money.ZERO))));
        return vat;
    }
}
