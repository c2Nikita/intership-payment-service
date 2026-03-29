package com.innowise.payment.controller;

import com.innowise.payment.dto.PaymentRequestDto;
import com.innowise.payment.dto.PaymentResponseDto;
import com.innowise.payment.entity.Status;
import com.innowise.payment.entity.TotalSumView;
import com.innowise.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponseDto createPayment(@Valid @RequestBody PaymentRequestDto requestDto) {
        return paymentService.createPayment(requestDto);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public List<PaymentResponseDto> searchPayments(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Status status) {
        return paymentService.searchPayments(userId, orderId, status);
    }

    @GetMapping("/summary/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public TotalSumView getTotalSumForUser(
            @PathVariable Long userId,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        return paymentService.getTotalSumForUser(userId, from, to);
    }

    @GetMapping("/summary/all")
    @PreAuthorize("hasRole('ADMIN')")
    public TotalSumView getTotalSumForAllUsers(
            @RequestParam Instant from,
            @RequestParam Instant to) {
        return paymentService.getTotalSumForAllUsers(from, to);
    }
}