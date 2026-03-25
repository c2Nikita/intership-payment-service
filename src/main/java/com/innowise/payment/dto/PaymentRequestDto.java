package com.innowise.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;


public record PaymentRequestDto(
        @NotNull(message = "Order ID is mandatory")
        Long orderId,

        @NotNull(message = "User ID is mandatory")
        Long userId,

        @NotNull(message = "Payment amount is mandatory")
        @Positive(message = "Payment amount must be greater than zero")
        BigDecimal paymentAmount
) {
}
