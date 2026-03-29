package com.innowise.payment.dto;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        BigDecimal paymentAmount
) {}