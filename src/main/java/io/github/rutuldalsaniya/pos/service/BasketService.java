package io.github.rutuldalsaniya.pos.service;

import io.github.rutuldalsaniya.pos.domain.Basket;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BasketService {

    private final Map<String, Basket> baskets = new ConcurrentHashMap<>();
    private final CatalogService catalog;

    public BasketService(CatalogService catalog) {
        this.catalog = catalog;
    }

    public Basket create() {
        Basket basket = new Basket(UUID.randomUUID().toString());
        baskets.put(basket.id(), basket);
        return basket;
    }

    public Basket get(String basketId) {
        Basket basket = baskets.get(basketId);
        if (basket == null) {
            throw new NotFoundException("Unknown basket " + basketId);
        }
        return basket;
    }

    public Basket addItem(String basketId, String sku, int quantity) {
        Basket basket = get(basketId);
        basket.add(catalog.get(sku), quantity);
        return basket;
    }

    public Basket removeItem(String basketId, String sku) {
        Basket basket = get(basketId);
        basket.remove(sku);
        return basket;
    }
}
