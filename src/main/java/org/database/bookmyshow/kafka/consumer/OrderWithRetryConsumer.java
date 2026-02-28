package org.database.bookmyshow.kafka.consumer;

import org.database.bookmyshow.kafka.entitiy.Orders;
import org.database.bookmyshow.kafka.event.OrderPlacedEvent;
import org.database.bookmyshow.kafka.reposiotry.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.database.bookmyshow.kafka.producer.OrderEventProducer.TOPIC_NAME;

/**
 * Consumer component responsible for processing Orders Placed events from Kafka.
 * Implements a non-blocking retry mechanism using Spring Kafka's @RetryableTopic.
 */
@Component
public class OrderWithRetryConsumer {

    /**
     * Mapper for JSON serialization/deserialization.
     */
    @Autowired
    private ObjectMapper objectMapper;

    public static final Logger log = LoggerFactory.getLogger(OrderWithRetryConsumer.class);

    /**
     * Repository for persisting order data to the database.
     */
    @Autowired
    private OrderRepository orderRepository;

    /**
     * Consumes messages from the 'orders' topic.
     *
     * @param event The JSON string payload of the order.
     * @param key The Kafka message key.
     * @param partition The partition from which the message was received.
     * @param offset The offset of the message in the partition.
     */

    /**
     *  attempts "3": Retry up to 3 times (total 4 attempts: 1 initial + 3 retries)
     * 'backoff': Wait time between retries delay = 1000: First retry after 1 second multiplier = 2.0: Each retry waits 2x longer (1s, 2s, 4s)
     * topicSuffixingStrategy: Creates retry topics like orders-retry-0, orders-retry-1, etc.
     * dltStrategy: What to do if all retries fail
     * include: Which exceptions trigger retry
     * */
    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            include = {RuntimeException.class}
    )
//    @KafkaListener(topics = TOPIC_NAME, groupId = "order-group")
//    @Transactional
    public void consumeOrderPlacedEvent(@Payload String event, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition, @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received OrderPlacedEvent: {}", event);
        log.info(" Partition: {}, Offset: {}",  partition, offset);

        try {
            OrderPlacedEvent orderEvent = objectMapper.readValue(event, OrderPlacedEvent.class);
            log.info("Received OrderPlacedEvent: orders Event {}", orderEvent);
            log.info("Received OrderPlacedEvent: orders Event total amount : {} , price {} ", orderEvent.totalAmount() , orderEvent.quantity());
            validateOrder(orderEvent);

            Orders orders = new Orders();
            orders.setCustomerId(orderEvent.customerId());
            orders.setProductId(orderEvent.productId());
            orders.setQuantity(orderEvent.quantity());
            orders.setTotalAmount(orderEvent.totalAmount());
            orders.setOrderDate(orderEvent.orderDate().toString());

//            orderRepository.save(orders);

            processOrder(orderEvent);
        } catch (Exception e) {
            log.error("Error processing order {}: {}", event, e.getMessage(), e);
            /*
             * Re-throwing the exception is crucial here.
             * It triggers the @RetryableTopic logic to move the message
             * to the retry topics or eventually the DLT.
             */
            throw new RuntimeException("Failed to process order: " + event, e);
        }
    }

    /**
     * Performs basic business validation on the incoming Orders event.
     *
     * @param event The order event to validate.
     * @throws IllegalArgumentException if validation constraints are violated.
     */
    private void validateOrder(OrderPlacedEvent event) {
        if (event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("Orders ID cannot be null or empty");
        }
        if (event.quantity() != null && event.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (event.totalAmount() != null && event.totalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be positive");
        }
    }

    /**
     * Executes business logic for a validated and persisted order.
     *
     * @param event The order event to process.
     */
    private void processOrder(OrderPlacedEvent event) {
        log.info("Processing order: {}", event.orderId());
        log.info("Customer: {}, Product: {}, Quantity: {}, Total: {}",
                event.customerId(),
                event.productId(),
                event.quantity(),
                event.totalAmount());

        // Placeholder for additional business logic (e.g., inventory update, notification)
        log.info("Orders {} processed successfully", event.orderId());
    }
}
