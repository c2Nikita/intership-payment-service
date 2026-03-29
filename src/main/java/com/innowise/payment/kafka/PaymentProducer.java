package com.innowise.payment.kafka;

import com.innowise.payment.dto.PaymentEvent;

public interface PaymentProducer {

    void sendPaymentEvent(PaymentEvent event);

}
