package com.innowise.payment.kafka;

import com.innowise.payment.dto.OrderCreatedEvent;

public interface OrderConsumer {
    void consumeOrderCreated(OrderCreatedEvent event);
}
