package com.innowise.payment.service;

import com.innowise.payment.dto.OrderCreatedEvent;
import com.innowise.payment.dto.PaymentRequestDto;
import com.innowise.payment.dto.PaymentResponseDto;
import com.innowise.payment.entity.Status;
import com.innowise.payment.entity.TotalSumView;

import java.time.Instant;
import java.util.List;

public interface PaymentService {
    PaymentResponseDto createPayment(PaymentRequestDto requestDto);

    List<PaymentResponseDto> searchPayments(Long userId, Long orderId, Status status);

    TotalSumView getTotalSumForUser(Long userId, Instant from, Instant to);

    TotalSumView getTotalSumForAllUsers(Instant from, Instant to);

    void processPaymentForOrder(OrderCreatedEvent event);
}
