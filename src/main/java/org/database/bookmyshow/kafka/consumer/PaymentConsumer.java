package org.database.bookmyshow.kafka.consumer;

import jakarta.transaction.Transactional;
import org.database.bookmyshow.kafka.event.OrderPlacedEvent;
import org.database.bookmyshow.kafka.reposiotry.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import tools.jackson.databind.ObjectMapper;

public class PaymentConsumer {
    public static final Logger log = LoggerFactory.getLogger(PaymentConsumer.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "orders", groupId = "order-group    ")
    @Transactional
    public void consumeOrderPlacedEvent(
            @Payload String event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received OrderPlacedEvent: {}", event);
        log.info("Key: {}, Offset: {}", key, offset);

        var orderEvent = objectMapper.readValue(event, OrderPlacedEvent.class);
        // Check if already processed (idempotency check)
        if (orderRepository.existsById(orderEvent.orderId())) {
            log.warn("Order {} already processed - skipping (idempotency)", orderEvent.orderId());
            return; // Already processed, skip
        }


        log.info("Order {} processed successfully", orderEvent.orderId());
    }
}
