package com.innowise.payment.repository;

import com.innowise.payment.entity.Payment;
import com.innowise.payment.entity.Status;

import java.util.List;

public interface PaymentSearchRepository {
    List<Payment> searchPayments(Long userId, Long orderId, Status status);
}
