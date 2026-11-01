package io.github.rutuldalsaniya.pos.web;

import io.github.rutuldalsaniya.pos.domain.Product;
import io.github.rutuldalsaniya.pos.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CatalogService catalog;

    public ProductController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public Collection<Product> all() {
        return catalog.findAll();
    }

    @GetMapping("/{sku}")
    public Product bySku(@PathVariable String sku) {
        return catalog.get(sku);
    }
}
