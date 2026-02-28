package org.database.bookmyshow.kafka.producer;


import org.database.bookmyshow.kafka.entitiy.PaymentProcessedEvent;
import org.database.bookmyshow.kafka.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class PaymentEventProducer {
    public static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);

    private static final String PAYMENT_TOPIC = "payments";
    private static final String ORDER_TOPIC = "orders";

    private final KafkaTemplate<String, String> kafkaTemplate;

    PaymentEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void processPaymentTransactionally(String orderId, String customerId, BigDecimal amount) {
        log.info("Starting transactional payment processing for order: {}", orderId);

        // Create payment event
        var paymentEvent = new PaymentProcessedEvent(UUID.randomUUID().toString(), orderId, customerId, amount, "PROCESSED", LocalDateTime.now());

//         Send to payments topic (within transaction)
        kafkaTemplate.send(PAYMENT_TOPIC, paymentEvent.paymentId(), paymentEvent.toString()).thenAccept(result -> {
            log.info("Payment event sent - PaymentId: {}, Partition: {}, Offset: {}", paymentEvent.paymentId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
        });

        // Send to orders topic (within same transaction)
        var orderUpdateEvent = new OrderPlacedEvent(orderId, customerId, "product-123", 1, amount, LocalDateTime.now());

        kafkaTemplate.send(ORDER_TOPIC, orderId, orderUpdateEvent.toString()).thenAccept(result -> {
            log.info("Order update sent - OrderId: {}, Partition: {}, Offset: {}", orderId, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
        });

        log.info("Transaction committed - Payment and order events sent atomically");
    }

}
