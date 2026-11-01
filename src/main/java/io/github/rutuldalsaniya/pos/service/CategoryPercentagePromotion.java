package io.github.rutuldalsaniya.pos.service;

import io.github.rutuldalsaniya.pos.domain.AppliedDiscount;
import io.github.rutuldalsaniya.pos.domain.Basket;
import io.github.rutuldalsaniya.pos.domain.BasketLine;
import io.github.rutuldalsaniya.pos.domain.Money;
import io.github.rutuldalsaniya.pos.domain.VatRate;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Example: "10 % off all bakery items". */
public class CategoryPercentagePromotion implements Promotion {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final String name;
    private final String category;
    private final BigDecimal percent;

    public CategoryPercentagePromotion(String name, String category, BigDecimal percent) {
        if (percent.signum() <= 0 || percent.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("percent must be in (0, 100]");
        }
        this.name = name;
        this.category = category;
        this.percent = percent;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<AppliedDiscount> apply(Basket basket) {
        Map<VatRate, BigDecimal> eligibleByRate = new EnumMap<>(VatRate.class);
        for (BasketLine line : basket.lines()) {
            if (category.equalsIgnoreCase(line.product().category())) {
                eligibleByRate.merge(line.product().vatRate(), line.lineTotal(), BigDecimal::add);
            }
        }
        return eligibleByRate.entrySet().stream()
                .map(e -> new AppliedDiscount(name,
                        Money.round(e.getValue().multiply(percent).divide(HUNDRED)),
                        e.getKey()))
                .filter(d -> d.amount().signum() > 0)
                .toList();
    }
}
