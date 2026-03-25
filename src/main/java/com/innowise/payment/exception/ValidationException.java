package com.innowise.payment.exception;

public class ValidationException extends RuntimeException {

    public ValidationException() {}
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Throwable reason) {
        super(reason);
    }

    public ValidationException(Throwable reason, String message) {
        super(message, reason);
    }
}
