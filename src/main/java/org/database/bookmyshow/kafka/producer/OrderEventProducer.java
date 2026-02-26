package org.database.bookmyshow.kafka.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.database.bookmyshow.kafka.event.OrderPlacedEvent;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import tools.jackson.databind.ObjectMapper;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderEventProducer {
    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    public static final String TOPIC_NAME = "order-topic";

    @Autowired
    private ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderPlaceEvent(OrderPlacedEvent event) {
        log.info("Sending order placed event to topic: {}", TOPIC_NAME);
        log.info("Orders placed event sent: {}", event);
        // Send message asynchronously

        String eventToString = objectMapper.writeValueAsString(event);
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC_NAME, eventToString);

        future.thenAccept(sendResult -> {
            var mataData = sendResult.getRecordMetadata();
            log.info("Message sent successfully! Topic: {}, Partition: {}, Offset: {}",
                    mataData.topic(),
                    mataData.partition(),
                    mataData.offset());

        });
        // Handle failure
        future.exceptionally(ex ->{
            log.error("Error sending message: {}", ex.getMessage());
            return null;
        });
    }


    public void sendBatch(List<OrderPlacedEvent> events) {
        List<CompletableFuture<SendResult<String, String>>> futures = new ArrayList<>();

        for (OrderPlacedEvent event : events) {
            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send("orders", event.orderId(), event.toString());
            futures.add(future);
        }

        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    log.info("All {} messages sent successfully", events.size());
                })
                .exceptionally(ex -> {
                    log.error("Some messages failed in batch", ex);
                    return null;
                });
    }


    // Scenario 6: With timestamp
    public void sendWithTimestamp(OrderPlacedEvent event) {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                "orders",
                null,
                event.orderDate().toEpochSecond(ZoneOffset.UTC) * 1000,
                event.orderId(),
                event.toString()
        );
        kafkaTemplate.send(record);
    }

    // Scenario 5: With headers
    public void sendWithHeaders(OrderPlacedEvent event) {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                "orders",
                event.orderId(),
                event.toString()
        );
        record.headers().add("correlation-id", event.orderId().getBytes());
        record.headers().add("source", "order-service".getBytes());
        kafkaTemplate.send(record);
    }
}
