package io.github.rutuldalsaniya.pos.service;

import io.github.rutuldalsaniya.pos.domain.Basket;
import io.github.rutuldalsaniya.pos.domain.BasketStatus;
import io.github.rutuldalsaniya.pos.domain.PaymentMethod;
import io.github.rutuldalsaniya.pos.domain.Receipt;
import io.github.rutuldalsaniya.pos.payment.PaymentGateway;
import io.github.rutuldalsaniya.pos.payment.PaymentRequest;
import io.github.rutuldalsaniya.pos.payment.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-01T10:15:30Z");

    @Mock
    private BasketService basketService;

    @Mock
    private PaymentGateway paymentGateway;

    private final CatalogService catalog = new CatalogService();
    private CheckoutService checkoutService;
    private Basket basket;

    @BeforeEach
    void setUp() {
        PricingService pricing = new PricingService(List.of());
        checkoutService = new CheckoutService(basketService, pricing, paymentGateway,
                Clock.fixed(NOW, ZoneOffset.UTC));
        basket = new Basket("b-42");
    }

    @Test
    void cardPaymentApprovedIssuesReceiptAndClosesBasket() {
        basket.add(catalog.get("1001"), 2); // 2.38
        when(basketService.get("b-42")).thenReturn(basket);
        when(paymentGateway.authorize(any())).thenReturn(PaymentResult.approved("TX-1"));

        Receipt receipt = checkoutService.checkout("b-42", PaymentMethod.CARD, null);

        ArgumentCaptor<PaymentRequest> request = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(paymentGateway).authorize(request.capture());
        assertThat(request.getValue().amount()).isEqualByComparingTo("2.38");
        assertThat(request.getValue().currency()).isEqualTo("EUR");

        assertThat(receipt.transactionId()).isEqualTo("TX-1");
        assertThat(receipt.timestamp()).isEqualTo(NOW);
        assertThat(receipt.pricing().total()).isEqualByComparingTo("2.38");
        assertThat(basket.status()).isEqualTo(BasketStatus.PAID);
    }

    @Test
    void declinedCardKeepsBasketOpen() {
        basket.add(catalog.get("5002"), 1);
        when(basketService.get("b-42")).thenReturn(basket);
        when(paymentGateway.authorize(any())).thenReturn(PaymentResult.declined("Card limit exceeded"));

        assertThatThrownBy(() -> checkoutService.checkout("b-42", PaymentMethod.CARD, null))
                .isInstanceOf(PaymentDeclinedException.class)
                .hasMessage("Card limit exceeded");

        assertThat(basket.status()).isEqualTo(BasketStatus.OPEN);
    }

    @Test
    void cashPaymentCalculatesChangeWithoutCallingCardGateway() {
        basket.add(catalog.get("2001"), 1); // 3.49
        when(basketService.get("b-42")).thenReturn(basket);

        Receipt receipt = checkoutService.checkout("b-42", PaymentMethod.CASH, new BigDecimal("5.00"));

        assertThat(receipt.change()).isEqualByComparingTo("1.51");
        assertThat(receipt.transactionId()).startsWith("CASH-");
        verifyNoInteractions(paymentGateway);
    }

    @Test
    void insufficientCashIsRejected() {
        basket.add(catalog.get("2001"), 1); // 3.49
        when(basketService.get("b-42")).thenReturn(basket);

        assertThatThrownBy(() -> checkoutService.checkout("b-42", PaymentMethod.CASH, new BigDecimal("3.00")))
                .isInstanceOf(PaymentDeclinedException.class);

        assertThat(basket.status()).isEqualTo(BasketStatus.OPEN);
        verifyNoInteractions(paymentGateway);
    }

    @Test
    void emptyBasketCannotBeCheckedOut() {
        when(basketService.get("b-42")).thenReturn(basket);

        assertThatThrownBy(() -> checkoutService.checkout("b-42", PaymentMethod.CARD, null))
                .isInstanceOf(CheckoutException.class)
                .hasMessageContaining("empty");

        verifyNoInteractions(paymentGateway);
    }

    @Test
    void paidBasketCannotBePaidTwice() {
        basket.add(catalog.get("1001"), 1);
        basket.markPaid();
        when(basketService.get("b-42")).thenReturn(basket);

        assertThatThrownBy(() -> checkoutService.checkout("b-42", PaymentMethod.CARD, null))
                .isInstanceOf(CheckoutException.class)
                .hasMessageContaining("already paid");
    }
}
