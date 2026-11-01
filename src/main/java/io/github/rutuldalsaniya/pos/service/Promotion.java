package io.github.rutuldalsaniya.pos.service;

import io.github.rutuldalsaniya.pos.domain.AppliedDiscount;
import io.github.rutuldalsaniya.pos.domain.Basket;

import java.util.List;

/**
 * A pricing rule that can grant discounts on a basket.
 * New promotion types are added by implementing this interface (open/closed principle).
 */
public interface Promotion {

    String name();

    List<AppliedDiscount> apply(Basket basket);
}
