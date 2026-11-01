package io.github.rutuldalsaniya.pos.service;

import io.github.rutuldalsaniya.pos.domain.Money;
import io.github.rutuldalsaniya.pos.domain.Product;
import io.github.rutuldalsaniya.pos.domain.VatRate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory article master data. In a real system this would come from
 * the retailer's product information system.
 */
@Service
public class CatalogService {

    private final Map<String, Product> products = new LinkedHashMap<>();

    public CatalogService() {
        add(new Product("1001", "Whole milk 1 l", Money.of("1.19"), VatRate.REDUCED, "DAIRY"));
        add(new Product("1002", "Natural yoghurt 500 g", Money.of("0.89"), VatRate.REDUCED, "DAIRY"));
        add(new Product("2001", "Sourdough bread 750 g", Money.of("3.49"), VatRate.REDUCED, "BAKERY"));
        add(new Product("2002", "Butter croissant", Money.of("0.99"), VatRate.REDUCED, "BAKERY"));
        add(new Product("3001", "Apples 1 kg", Money.of("2.29"), VatRate.REDUCED, "PRODUCE"));
        add(new Product("4001", "Milk chocolate 100 g", Money.of("1.29"), VatRate.REDUCED, "SWEETS"));
        add(new Product("5001", "Shampoo 300 ml", Money.of("2.95"), VatRate.STANDARD, "DRUGSTORE"));
        add(new Product("5002", "Kitchen towels 4 rolls", Money.of("3.79"), VatRate.STANDARD, "HOUSEHOLD"));
    }

    private void add(Product product) {
        products.put(product.sku(), product);
    }

    public Collection<Product> findAll() {
        return products.values();
    }

    public Optional<Product> find(String sku) {
        return Optional.ofNullable(products.get(sku));
    }

    public Product get(String sku) {
        return find(sku).orElseThrow(() -> new NotFoundException("Unknown article " + sku));
    }
}
