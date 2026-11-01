package io.github.rutuldalsaniya.pos.service;

import io.github.rutuldalsaniya.pos.domain.Basket;
import io.github.rutuldalsaniya.pos.domain.Money;
import io.github.rutuldalsaniya.pos.domain.PricedBasket;
import io.github.rutuldalsaniya.pos.domain.VatRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceTest {

    private final CatalogService catalog = new CatalogService();
    private PricingService pricing;
    private Basket basket;

    @BeforeEach
    void setUp() {
        pricing = new PricingService(List.of(
                new CategoryPercentagePromotion("Bakery -10%", "BAKERY", new BigDecimal("10")),
                new MultiBuyPromotion("Chocolate 3 for 2", "4001", 3, 2)));
        basket = new Basket("b-1");
    }

    @Test
    @DisplayName("Subtotal is the sum of all line totals")
    void subtotalSumsLines() {
        basket.add(catalog.get("1001"), 2); // 2 x 1.19
        basket.add(catalog.get("5001"), 1); // 1 x 2.95

        PricedBasket result = pricing.price(basket);

        assertThat(result.subtotal()).isEqualByComparingTo("5.33");
        assertThat(result.discounts()).isEmpty();
        assertThat(result.total()).isEqualByComparingTo("5.33");
    }

    @Test
    @DisplayName("Scanning the same article twice merges into one line")
    void sameArticleIsMerged() {
        basket.add(catalog.get("1001"), 1);
        basket.add(catalog.get("1001"), 2);

        assertThat(basket.lines()).hasSize(1);
        assertThat(basket.lines().get(0).quantity()).isEqualTo(3);
    }

    @ParameterizedTest(name = "{0} chocolates -> discount {1}")
    @CsvSource({"2, 0.00", "3, 1.29", "5, 1.29", "6, 2.58"})
    void threeForTwo(int quantity, String expectedDiscount) {
        basket.add(catalog.get("4001"), quantity);

        PricedBasket result = pricing.price(basket);

        BigDecimal discount = result.subtotal().subtract(result.total());
        assertThat(discount).isEqualByComparingTo(expectedDiscount);
    }

    @Test
    @DisplayName("Category discount applies only to bakery items")
    void categoryDiscountOnlyForCategory() {
        basket.add(catalog.get("2001"), 2); // bread 2 x 3.49 = 6.98 -> 10% = 0.70
        basket.add(catalog.get("1001"), 1); // milk, no discount

        PricedBasket result = pricing.price(basket);

        assertThat(result.discounts()).singleElement()
                .satisfies(d -> assertThat(d.amount()).isEqualByComparingTo("0.70"));
        assertThat(result.total()).isEqualByComparingTo("7.47");
    }

    @Test
    @DisplayName("VAT is split by rate and calculated after discounts")
    void vatBreakdownAfterDiscounts() {
        basket.add(catalog.get("2001"), 1); // 3.49 at 7%, minus 0.35 bakery discount = 3.14
        basket.add(catalog.get("5001"), 1); // 2.95 at 19%

        PricedBasket result = pricing.price(basket);

        // 3.14 * 7 / 107 = 0.205... -> 0.21 ; 2.95 * 19 / 119 = 0.470... -> 0.47
        assertThat(result.vatBreakdown().get(VatRate.REDUCED)).isEqualByComparingTo("0.21");
        assertThat(result.vatBreakdown().get(VatRate.STANDARD)).isEqualByComparingTo("0.47");
    }

    @Test
    @DisplayName("Empty basket prices to zero")
    void emptyBasket() {
        PricedBasket result = pricing.price(basket);

        assertThat(result.total()).isEqualByComparingTo(Money.ZERO);
        assertThat(result.vatBreakdown()).isEmpty();
    }
}
