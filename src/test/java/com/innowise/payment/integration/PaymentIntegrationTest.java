package com.innowise.payment.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.innowise.payment.dto.PaymentEvent;
import com.innowise.payment.dto.PaymentRequestDto;
import com.innowise.payment.dto.PaymentResponseDto;
import com.innowise.payment.entity.Payment;
import com.innowise.payment.entity.Status;
import com.innowise.payment.repository.PaymentRepository;
import com.innowise.payment.service.PaymentService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = "spring.autoconfigure.exclude=com.innowise.security.SecurityAutoConfiguration"
)
@Testcontainers
@ActiveProfiles("test")
class PaymentIntegrationTest {

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";
    private static final Duration CONSUMER_TIMEOUT = Duration.ofSeconds(10);

    @Container
    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:7.0");

    @Container
    static final KafkaContainer KAFKA_CONTAINER =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @RegisterExtension
    static final WireMockExtension WIRE_MOCK =
            WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
        registry.add("spring.kafka.producer.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("app.external-api.random-number.base-url", WIRE_MOCK::baseUrl);
    }

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        WIRE_MOCK.resetAll();
    }

    @Test
    void createPaymentPersistsSuccessAndPublishesKafkaEvent() {
        WIRE_MOCK.stubFor(get(urlEqualTo("/api/v1.0/random")).willReturn(okJson("[2]")));

        PaymentResponseDto created = paymentService.createPayment(
                new PaymentRequestDto(1001L, 2001L, new BigDecimal("125.50"))
        );
        String paymentId = created.id();
        assertThat(created.status()).isEqualTo(Status.SUCCESS);

        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);
        assertThat(paymentOptional).isPresent();
        Payment savedPayment = paymentOptional.orElseThrow();
        assertThat(savedPayment.getOrderId()).isEqualTo(1001L);
        assertThat(savedPayment.getUserId()).isEqualTo(2001L);
        assertThat(savedPayment.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("125.50"));
        assertThat(savedPayment.getStatus()).isEqualTo(Status.SUCCESS);

        PaymentEvent consumedEvent = consumeSinglePaymentEvent(1001L);
        assertThat(consumedEvent.paymentId()).isEqualTo(paymentId);
        assertThat(consumedEvent.orderId()).isEqualTo(1001L);
        assertThat(consumedEvent.status()).isEqualTo(Status.SUCCESS);
        assertThat(consumedEvent.timestamp()).isNotNull();
    }

    @Test
    void createPaymentPersistsFailedWhenRandomNumberIsOdd() {
        WIRE_MOCK.stubFor(get(urlEqualTo("/api/v1.0/random")).willReturn(okJson("[3]")));

        PaymentResponseDto created = paymentService.createPayment(
                new PaymentRequestDto(1002L, 2002L, new BigDecimal("44.00"))
        );
        String paymentId = created.id();
        assertThat(created.status()).isEqualTo(Status.FAILED);

        Payment savedPayment = paymentRepository.findById(paymentId).orElseThrow();
        assertThat(savedPayment.getStatus()).isEqualTo(Status.FAILED);
        assertThat(savedPayment.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("44.00"));

        PaymentEvent consumedEvent = consumeSinglePaymentEvent(1002L);
        assertThat(consumedEvent.paymentId()).isEqualTo(paymentId);
        assertThat(consumedEvent.orderId()).isEqualTo(1002L);
        assertThat(consumedEvent.status()).isEqualTo(Status.FAILED);
    }

    private PaymentEvent consumeSinglePaymentEvent(Long orderId) {
        Map<String, Object> consumerConfig = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "payment-integration-test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class,
                JacksonJsonDeserializer.TRUSTED_PACKAGES, "*",
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, PaymentEvent.class.getName()
        );

        try (KafkaConsumer<String, PaymentEvent> consumer = new KafkaConsumer<>(consumerConfig)) {
            consumer.subscribe(java.util.List.of(PAYMENT_EVENTS_TOPIC));
            long deadline = System.currentTimeMillis() + CONSUMER_TIMEOUT.toMillis();

            while (System.currentTimeMillis() < deadline) {
                ConsumerRecords<String, PaymentEvent> records = consumer.poll(Duration.ofMillis(250));
                for (ConsumerRecord<String, PaymentEvent> record : records) {
                    PaymentEvent event = record.value();
                    if (event != null && orderId.equals(event.orderId())) {
                        return event;
                    }
                }
            }
        }

        throw new AssertionError("Payment event was not published for orderId=" + orderId);
    }
}
