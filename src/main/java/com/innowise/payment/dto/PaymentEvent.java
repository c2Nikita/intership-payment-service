package com.innowise.payment.dto;

import com.innowise.payment.entity.Status;

import java.time.Instant;

public record PaymentEvent(
        Long orderId,
        String paymentId,
        Status status,
        Instant timestamp
) {}
