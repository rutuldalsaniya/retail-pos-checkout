package io.github.rutuldalsaniya.pos.payment;

public record PaymentResult(boolean approved, String transactionId, String message) {

    public static PaymentResult approved(String transactionId) {
        return new PaymentResult(true, transactionId, "Approved");
    }

    public static PaymentResult declined(String message) {
        return new PaymentResult(false, null, message);
    }
}
