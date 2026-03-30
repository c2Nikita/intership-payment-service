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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private RandomNumberClient randomNumberClient;

    @Mock
    private PaymentProducer paymentProducer;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void createPayment_Success() {
        PaymentRequestDto requestDto = new PaymentRequestDto(1L, 2L, BigDecimal.valueOf(100));
        Payment payment = new Payment();
        payment.setOrderId(1L);

        Payment savedPayment = new Payment();
        savedPayment.setId("p1");
        savedPayment.setOrderId(1L);
        savedPayment.setStatus(Status.SUCCESS);
        savedPayment.setTimestamp(Instant.now());

        PaymentResponseDto responseDto = new PaymentResponseDto("p1", 1L, 2L, Status.SUCCESS, Instant.now(), BigDecimal.valueOf(100));

        when(paymentMapper.toEntity(requestDto)).thenReturn(payment);
        when(randomNumberClient.generateRandomNumberArray()).thenReturn(new Integer[]{4});
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentMapper.toDto(savedPayment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.createPayment(requestDto);

        assertNotNull(result);
        assertEquals(Status.SUCCESS, result.status());
        verify(paymentRepository, times(1)).save(payment);
        verify(paymentProducer, times(1)).sendPaymentEvent(any(PaymentEvent.class));
    }

    @Test
    void createPayment_FailedStatus() {
        PaymentRequestDto requestDto = new PaymentRequestDto(1L, 2L, BigDecimal.valueOf(100));
        Payment payment = new Payment();
        payment.setOrderId(1L);

        Payment savedPayment = new Payment();
        savedPayment.setId("p2");
        savedPayment.setStatus(Status.FAILED);
        savedPayment.setTimestamp(Instant.now());

        PaymentResponseDto responseDto = new PaymentResponseDto("p2", 1L, 2L, Status.FAILED, Instant.now(), BigDecimal.valueOf(100));

        when(paymentMapper.toEntity(requestDto)).thenReturn(payment);
        when(randomNumberClient.generateRandomNumberArray()).thenReturn(new Integer[]{5});
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentMapper.toDto(savedPayment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.createPayment(requestDto);

        assertEquals(Status.FAILED, result.status());
        verify(paymentProducer, times(1)).sendPaymentEvent(any(PaymentEvent.class));
    }

    @Test
    void createPayment_NullDtoThrowsException() {
        assertThrows(ValidationException.class, () -> paymentService.createPayment(null));
        verifyNoInteractions(paymentRepository, paymentProducer, randomNumberClient);
    }

    @Test
    void searchPayments_ReturnsList() {
        Long userId = 1L;
        Long orderId = 2L;
        Status status = Status.SUCCESS;

        Payment payment = new Payment();
        List<Payment> payments = List.of(payment);
        PaymentResponseDto responseDto = new PaymentResponseDto("p1", 2L, 1L, Status.SUCCESS, Instant.now(), BigDecimal.TEN);
        List<PaymentResponseDto> responseDtos = List.of(responseDto);

        when(paymentRepository.searchPayments(userId, orderId, status)).thenReturn(payments);
        when(paymentMapper.toDtoList(payments)).thenReturn(responseDtos);

        List<PaymentResponseDto> result = paymentService.searchPayments(userId, orderId, status);

        assertEquals(1, result.size());
        verify(paymentRepository, times(1)).searchPayments(userId, orderId, status);
    }

    @Test
    void getTotalSumForUser_Success() {
        Long userId = 1L;
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        TotalSumView expectedView = new TotalSumView(BigDecimal.valueOf(500));

        when(paymentRepository.getTotalSumByUserIdAndDateRange(userId, from, to)).thenReturn(expectedView);

        TotalSumView result = paymentService.getTotalSumForUser(userId, from, to);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(500), result.total());
    }

    @Test
    void getTotalSumForUser_NullIdThrowsException() {
        assertThrows(ValidationException.class, () -> paymentService.getTotalSumForUser(null, Instant.now(), Instant.now()));
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void getTotalSumForAllUsers_Success() {
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        TotalSumView expectedView = new TotalSumView(BigDecimal.valueOf(1000));

        when(paymentRepository.getTotalSumByDateRange(from, to)).thenReturn(expectedView);

        TotalSumView result = paymentService.getTotalSumForAllUsers(from, to);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1000), result.total());
    }

}