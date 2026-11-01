package io.github.rutuldalsaniya.pos.payment;

/**
 * Port to an external card payment provider (payment terminal / PSP).
 * The checkout logic depends only on this interface, so providers can be
 * swapped and the logic can be unit-tested with a mock.
 */
public interface PaymentGateway {

    PaymentResult authorize(PaymentRequest request);
}
