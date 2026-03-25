package com.innowise.payment.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException() {}

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Throwable reason) {
        super(reason);
    }

    public NotFoundException(String message, Throwable reason) {
        super(message, reason);
    }
}
