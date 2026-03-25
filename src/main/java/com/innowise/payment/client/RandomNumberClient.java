package com.innowise.payment.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "random-number-client", url = "${app.external-api.random-number.base-url}")
public interface RandomNumberClient {

    @GetMapping("/api/v1.0/random")
    @CircuitBreaker(name = "randomNumberBreaker", fallbackMethod = "getRandomNumberFallback")
    Integer[] generateRandomNumberArray();

    default Integer[] getRandomNumberFallback(Throwable t) {
        return new Integer[]{1};
    }
}