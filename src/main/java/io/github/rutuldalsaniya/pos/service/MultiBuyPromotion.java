package io.github.rutuldalsaniya.pos.service;

import io.github.rutuldalsaniya.pos.domain.AppliedDiscount;
import io.github.rutuldalsaniya.pos.domain.Basket;
import io.github.rutuldalsaniya.pos.domain.Money;

import java.math.BigDecimal;
import java.util.List;

/** Example: "3 for 2" on one article - buy {@code buy}, pay for {@code payFor}. */
public class MultiBuyPromotion implements Promotion {

    private final String name;
    private final String sku;
    private final int buy;
    private final int payFor;

    public MultiBuyPromotion(String name, String sku, int buy, int payFor) {
        if (buy < 2 || payFor < 1 || payFor >= buy) {
            throw new IllegalArgumentException("Require buy >= 2 and 1 <= payFor < buy");
        }
        this.name = name;
        this.sku = sku;
        this.buy = buy;
        this.payFor = payFor;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<AppliedDiscount> apply(Basket basket) {
        return basket.line(sku)
                .filter(line -> line.quantity() >= buy)
                .map(line -> {
                    int freeItems = (line.quantity() / buy) * (buy - payFor);
                    BigDecimal amount = Money.round(line.product().unitPrice().multiply(BigDecimal.valueOf(freeItems)));
                    return List.of(new AppliedDiscount(name, amount, line.product().vatRate()));
                })
                .orElse(List.of());
    }
}
