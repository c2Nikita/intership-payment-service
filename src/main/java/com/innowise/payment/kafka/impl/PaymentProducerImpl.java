package com.innowise.payment.kafka.impl;

import com.innowise.payment.dto.PaymentEvent;
import com.innowise.payment.kafka.PaymentProducer;
import org.springframework.stereotype.Component;
import org.springframework.kafka.core.KafkaTemplate;

@Component
public class PaymentProducerImpl implements PaymentProducer {

    private static final String TOPIC = "payment-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentProducerImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    public void sendPaymentEvent(PaymentEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.orderId()), event);
    }
}
