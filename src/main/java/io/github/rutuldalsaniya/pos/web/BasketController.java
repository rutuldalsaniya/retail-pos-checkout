package io.github.rutuldalsaniya.pos.web;

import io.github.rutuldalsaniya.pos.domain.Basket;
import io.github.rutuldalsaniya.pos.service.BasketService;
import io.github.rutuldalsaniya.pos.service.CheckoutService;
import io.github.rutuldalsaniya.pos.service.PricingService;
import io.github.rutuldalsaniya.pos.web.BasketDtos.AddItemRequest;
import io.github.rutuldalsaniya.pos.web.BasketDtos.BasketResponse;
import io.github.rutuldalsaniya.pos.web.BasketDtos.CheckoutRequest;
import io.github.rutuldalsaniya.pos.web.BasketDtos.ReceiptResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/baskets")
public class BasketController {

    private final BasketService basketService;
    private final PricingService pricingService;
    private final CheckoutService checkoutService;

    public BasketController(BasketService basketService, PricingService pricingService,
                            CheckoutService checkoutService) {
        this.basketService = basketService;
        this.pricingService = pricingService;
        this.checkoutService = checkoutService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BasketResponse create() {
        return toResponse(basketService.create());
    }

    @GetMapping("/{basketId}")
    public BasketResponse get(@PathVariable String basketId) {
        return toResponse(basketService.get(basketId));
    }

    @PostMapping("/{basketId}/items")
    public BasketResponse addItem(@PathVariable String basketId, @Valid @RequestBody AddItemRequest request) {
        return toResponse(basketService.addItem(basketId, request.sku(), request.quantity()));
    }

    @DeleteMapping("/{basketId}/items/{sku}")
    public BasketResponse removeItem(@PathVariable String basketId, @PathVariable String sku) {
        return toResponse(basketService.removeItem(basketId, sku));
    }

    @PostMapping("/{basketId}/checkout")
    public ReceiptResponse checkout(@PathVariable String basketId, @Valid @RequestBody CheckoutRequest request) {
        return ReceiptResponse.from(
                checkoutService.checkout(basketId, request.paymentMethod(), request.cashTendered()));
    }

    private BasketResponse toResponse(Basket basket) {
        return BasketResponse.from(basket, pricingService.price(basket));
    }
}
