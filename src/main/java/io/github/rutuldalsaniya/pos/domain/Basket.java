package io.github.rutuldalsaniya.pos.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A customer's basket at the till. Lines are kept in scan order;
 * scanning the same article again increases its quantity.
 */
public class Basket {

    private final String id;
    private final Map<String, BasketLine> lines = new LinkedHashMap<>();
    private BasketStatus status = BasketStatus.OPEN;

    public Basket(String id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public String id() {
        return id;
    }

    public BasketStatus status() {
        return status;
    }

    public synchronized void add(Product product, int quantity) {
        requireOpen();
        lines.merge(product.sku(), new BasketLine(product, quantity),
                (existing, added) -> existing.withQuantity(existing.quantity() + added.quantity()));
    }

    public synchronized void remove(String sku) {
        requireOpen();
        lines.remove(sku);
    }

    public synchronized List<BasketLine> lines() {
        return List.copyOf(new ArrayList<>(lines.values()));
    }

    public synchronized Optional<BasketLine> line(String sku) {
        return Optional.ofNullable(lines.get(sku));
    }

    public synchronized boolean isEmpty() {
        return lines.isEmpty();
    }

    public synchronized void markPaid() {
        requireOpen();
        status = BasketStatus.PAID;
    }

    private void requireOpen() {
        if (status != BasketStatus.OPEN) {
            throw new IllegalStateException("Basket " + id + " is already " + status);
        }
    }
}
