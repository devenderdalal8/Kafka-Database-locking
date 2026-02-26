package org.database.bookmyshow.kafka.consumer;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;


import static org.database.bookmyshow.kafka.producer.OrderEventProducer.TOPIC_NAME;

@Component
public class OrderConsumer {
    public static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);


    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @KafkaListener(topics = TOPIC_NAME, groupId = "order-group")
    public void consumerOrderPlacedEvent(
            @Payload String event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received OrderPlacedEvent: {}", event);
        log.info("Partition: {}, Offset: {}", partition, offset);

        processOrder(event);
    }

    private void processOrder(String event) {
        log.info("Processing order: {}", event);


        // Additional business logic here
        log.info("Orders {} processed successfully", event);
        // Here you would typically:
        // 1. Validate the order
        // 2. Update inventory
        // 3. Send confirmation email
        // 4. Update database
        // 5. Trigger other business logic

        log.info("Orders {} processed successfully", event);
    }


}
