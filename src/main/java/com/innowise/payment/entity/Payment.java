package com.innowise.payment.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Document(collection = "payments")
public class Payment {

    private String id;

    @Field("order_id")
    private Long orderId;

    @Field("user_id")
    private Long userId;

    private Status status;

    private Instant timestamp;

    @Field(name = "payment_amount", targetType = FieldType.DECIMAL128)
    private BigDecimal paymentAmount;

}
