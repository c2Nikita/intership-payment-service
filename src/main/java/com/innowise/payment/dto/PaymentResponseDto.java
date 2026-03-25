package com.innowise.payment.dto;

import com.innowise.payment.entity.Status;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponseDto(
        String id,
        Long orderId,
        Long userId,
        Status status,
        Instant timestamp,
        BigDecimal paymentAmount
) {
}
