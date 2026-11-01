package io.github.rutuldalsaniya.pos.web;

import io.github.rutuldalsaniya.pos.domain.Basket;
import io.github.rutuldalsaniya.pos.domain.Money;
import io.github.rutuldalsaniya.pos.domain.PaymentMethod;
import io.github.rutuldalsaniya.pos.domain.PricedBasket;
import io.github.rutuldalsaniya.pos.service.BasketService;
import io.github.rutuldalsaniya.pos.service.CheckoutService;
import io.github.rutuldalsaniya.pos.service.NotFoundException;
import io.github.rutuldalsaniya.pos.service.PaymentDeclinedException;
import io.github.rutuldalsaniya.pos.service.PricingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BasketController.class)
class BasketControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BasketService basketService;

    @MockBean
    private PricingService pricingService;

    @MockBean
    private CheckoutService checkoutService;

    private static PricedBasket emptyPricing() {
        return new PricedBasket(List.of(), Money.ZERO, List.of(), Money.ZERO, Map.of());
    }

    @Test
    void createBasketReturns201() throws Exception {
        Basket basket = new Basket("b-1");
        when(basketService.create()).thenReturn(basket);
        when(pricingService.price(basket)).thenReturn(emptyPricing());

        mvc.perform(post("/api/baskets"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("b-1"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void unknownBasketReturns404() throws Exception {
        when(basketService.get("missing")).thenThrow(new NotFoundException("Unknown basket missing"));

        mvc.perform(get("/api/baskets/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Unknown basket missing"));
    }

    @Test
    void addItemWithInvalidQuantityReturns400() throws Exception {
        mvc.perform(post("/api/baskets/b-1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"1001\",\"quantity\":0}"))
                .andExpect(status().isBadRequest());

        verify(basketService, never()).addItem(any(), any(), eq(0));
    }

    @Test
    void declinedPaymentReturns402() throws Exception {
        when(checkoutService.checkout(eq("b-1"), eq(PaymentMethod.CARD), any()))
                .thenThrow(new PaymentDeclinedException("Card limit exceeded"));

        mvc.perform(post("/api/baskets/b-1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"CARD\"}"))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.detail").value("Card limit exceeded"));
    }
}
