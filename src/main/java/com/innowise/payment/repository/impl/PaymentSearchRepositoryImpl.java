package com.innowise.payment.repository.impl;

import com.innowise.payment.entity.Payment;
import com.innowise.payment.entity.Status;
import com.innowise.payment.repository.PaymentSearchRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;


import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PaymentSearchRepositoryImpl implements PaymentSearchRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Payment> searchPayments(Long userId, Long orderId, Status status) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (userId != null) {
            criteriaList.add(Criteria.where("user_id").is(userId));
        }
        if (orderId != null) {
            criteriaList.add(Criteria.where("order_id").is(orderId));
        }
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().orOperator(criteriaList.toArray(new Criteria[0])));
        }

        return mongoTemplate.find(query, Payment.class);
    }
}
