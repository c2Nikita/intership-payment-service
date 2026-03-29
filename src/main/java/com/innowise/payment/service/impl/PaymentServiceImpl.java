package com.innowise.payment.service.impl;

import com.innowise.payment.client.RandomNumberClient;
import com.innowise.payment.dto.PaymentEvent;
import com.innowise.payment.dto.PaymentRequestDto;
import com.innowise.payment.dto.PaymentResponseDto;
import com.innowise.payment.entity.Payment;
import com.innowise.payment.entity.Status;
import com.innowise.payment.entity.TotalSumView;
import com.innowise.payment.exception.ValidationException;
import com.innowise.payment.mapper.PaymentMapper;
import com.innowise.payment.repository.PaymentRepository;
import com.innowise.payment.kafka.PaymentProducer;
import com.innowise.payment.service.PaymentService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String PAYMENT_DTO_MUST_NOT_BE_NULL = "Payment dto must not be null";
    private static final String USER_ID_MUST_NOT_BE_NULL = "User id must not be null";

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    private final RandomNumberClient randomNumberClient;

    private final PaymentProducer paymentProducer;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              PaymentMapper paymentMapper,
                              RandomNumberClient randomNumberClient,
                              PaymentProducer paymentProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.randomNumberClient = randomNumberClient;
        this.paymentProducer = paymentProducer;
    }

    @Override
    public PaymentResponseDto createPayment(PaymentRequestDto requestDto) {
        validateDto(requestDto);

        Payment payment = paymentMapper.toEntity(requestDto);
        payment.setTimestamp(Instant.now());

        Integer[] randomResponse = randomNumberClient.generateRandomNumberArray();
        int randomNumber = (randomResponse != null && randomResponse.length > 0) ? randomResponse[0] : 1;

        if(randomNumber % 2 == 0) {
            payment.setStatus(Status.SUCCESS);
        } else {
            payment.setStatus(Status.FAILED);
        }

        Payment savedPayment = paymentRepository.save(payment);

        PaymentEvent event = new PaymentEvent(
                savedPayment.getOrderId(),
                savedPayment.getId(),
                savedPayment.getStatus(),
                savedPayment.getTimestamp()
        );
        paymentProducer.sendPaymentEvent(event);

        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public List<PaymentResponseDto> searchPayments(Long userId, Long orderId, Status status) {
        List<Payment> payments = paymentRepository.searchPayments(userId, orderId, status);
        return paymentMapper.toDtoList(payments);
    }

    @Override
    public TotalSumView getTotalSumForUser(Long userId, Instant from, Instant to) {
        validateId(userId);
        return paymentRepository.getTotalSumByUserIdAndDateRange(userId, from, to);
    }

    @Override
    public TotalSumView getTotalSumForAllUsers(Instant from, Instant to) {
        return paymentRepository.getTotalSumByDateRange(from, to);
    }


    private void validateId(Long userId) {
        if(userId == null) {
            throw new ValidationException(USER_ID_MUST_NOT_BE_NULL);
        }
    }

    private void validateDto(PaymentRequestDto paymentRequestDto) {
        if(paymentRequestDto == null) {
            throw new ValidationException(PAYMENT_DTO_MUST_NOT_BE_NULL);
        }
    }
}
