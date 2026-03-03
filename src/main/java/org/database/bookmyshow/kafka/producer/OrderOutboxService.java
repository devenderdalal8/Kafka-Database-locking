package org.database.bookmyshow.kafka.producer;

import org.apache.kafka.common.protocol.types.Field;
import org.database.bookmyshow.kafka.entitiy.OrderOutboxEvent;
import org.database.bookmyshow.kafka.entitiy.OutboxStatus;
import org.database.bookmyshow.kafka.reposiotry.OrderOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderOutboxService {

    public static final Logger log = LoggerFactory.getLogger(OrderOutboxService.class);
    public static final int MAX_RETRIES = 4;

    private OrderOutboxRepository orderOutboxRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    OrderOutboxService(OrderOutboxRepository orderOutboxRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.orderOutboxRepository = orderOutboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Save event to outbox table (called within transaction)
     */
    @Transactional
    public void saveToOutbox(
            String aggregateId,
            String aggregateType,
            String eventType,
            String eventVersion,
            Object event,
            String partitionKey
    ) {
        log.info("Saving order outbox event to kafka for {}", aggregateId);
        try {
            String payload = objectMapper.writeValueAsString(event);
            var outboxEvent = new OrderOutboxEvent(
                    aggregateId,
                    aggregateType,
                    eventType,
                    eventVersion,
                    payload,
                    partitionKey
            );
            orderOutboxRepository.save(outboxEvent);
            log.info("Saved order outbox event to Kafka for aggregateId {}, EventType {}", aggregateId, eventType);
        } catch (Exception e) {
            log.error("Failed to save event to outbox: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save to outbox", e);
        }
    }

    /**
     * Publish pending events from outbox to Kafka
     */
    @Transactional
    public void publishPendingEvents() {
        List<OrderOutboxEvent> orderOutboxEvents = orderOutboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        log.info("Publishing order outbox events to kafka with size {}", orderOutboxEvents.size());

        for (OrderOutboxEvent orderOutboxEvent : orderOutboxEvents) {
            try {
                publishEvent(orderOutboxEvent);
            } catch (Exception e) {
                log.error("Failed to publish event {}: {}", orderOutboxEvent.getId(), e.getMessage(), e);
                orderOutboxRepository.markedAsFailed(orderOutboxEvent.getId(), e.getMessage());
            }
        }

        // Also try failed events
        List<OrderOutboxEvent> failedEvents = orderOutboxRepository.findFailedEventForRetry(MAX_RETRIES);
        log.info("Publishing failed order outbox events to kafka with size {}", failedEvents.size());

        for (OrderOutboxEvent orderOutboxEvent : failedEvents) {
            try {
                publishEvent(orderOutboxEvent);
            } catch (Exception e) {
                log.error("Failed to publish event {}: {}", orderOutboxEvent.getId(), e.getMessage(), e);
                orderOutboxRepository.markedAsFailed(orderOutboxEvent.getId(), e.getMessage());

            }
        }
    }

    private void publishEvent(OrderOutboxEvent event) {
        String topic = determineTopic(event.getEventType());
        String key = event.getPartitionKey() != null ? event.getPartitionKey() : event.getAggregateId();
        log.info("Publishing order outbox event {} to topic {}", event, topic);

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, event.getPayload());

        future.thenAccept(result -> {
            orderOutboxRepository.markedAsPublished(event.getId(), LocalDateTime.now());
            log.info("Event {} published successfully to topic {}, partition {}, offset {}",
                    event.getId(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        }).exceptionally(ex -> {
            log.error("Failed to publish event {}: {}", event.getId(), ex.getMessage());
            orderOutboxRepository.markedAsFailed(event.getId(), ex.getMessage());
            return null;
        });

        // Wait for completion (in production, you might want async)
        try {
            future.get(); // Block until published
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    private String determineTopic(String eventType) {
        // Map event types to topics
        return switch (eventType) {
            case "OrderPlaced" -> "orders";
            case "PaymentProcessed" -> "payments";
            case "OrderShipped" -> "orders";
            default -> "events"; // Default topic
        };
    }
}
