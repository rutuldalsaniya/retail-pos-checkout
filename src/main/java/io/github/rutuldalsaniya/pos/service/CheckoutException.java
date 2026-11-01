package io.github.rutuldalsaniya.pos.service;

/** The basket cannot be checked out in its current state. */
public class CheckoutException extends RuntimeException {

    public CheckoutException(String message) {
        super(message);
    }
}
