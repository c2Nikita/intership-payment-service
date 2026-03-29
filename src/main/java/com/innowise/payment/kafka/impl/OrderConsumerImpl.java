package com.innowise.payment.kafka.impl;


import com.innowise.payment.dto.OrderCreatedEvent;
import com.innowise.payment.kafka.OrderConsumer;
import com.innowise.payment.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumerImpl implements OrderConsumer {

    private final PaymentService paymentService;

    public OrderConsumerImpl(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        paymentService.processPaymentForOrder(event);
    }
}