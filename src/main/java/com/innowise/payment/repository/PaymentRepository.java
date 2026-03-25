package com.innowise.payment.repository;

import com.innowise.payment.entity.Payment;
import com.innowise.payment.entity.TotalSumView;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String>, PaymentSearchRepository {

    @Aggregation(pipeline = {
            "{ $match: { 'user_id': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } } }",
            "{ $group: { '_id': null, 'total': { $sum: '$payment_amount' } } }"
    })
    TotalSumView getTotalSumByUserIdAndDateRange(Long userId, Instant from, Instant to);
    @Aggregation(pipeline = {
            "{ $match: { 'timestamp': { $gte: ?0, $lte: ?1 } } }",
            "{ $group: { '_id': null, 'total': { $sum: '$payment_amount' } } }"
    })
    TotalSumView getTotalSumByDateRange(Instant from, Instant to);

}
