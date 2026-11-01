package io.github.rutuldalsaniya.pos.web;

import io.github.rutuldalsaniya.pos.service.CheckoutException;
import io.github.rutuldalsaniya.pos.service.NotFoundException;
import io.github.rutuldalsaniya.pos.service.PaymentDeclinedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps domain errors to RFC 7807 problem responses. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({CheckoutException.class, IllegalStateException.class})
    ProblemDetail conflict(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(PaymentDeclinedException.class)
    ProblemDetail paymentDeclined(PaymentDeclinedException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.PAYMENT_REQUIRED, e.getMessage());
    }
}
