package org.database.bookmyshow.kafka.consumer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Component
public class OrderDeadLetterConsumer {

    public static final Logger log = LoggerFactory.getLogger(OrderDeadLetterConsumer.class.getName());

    @KafkaListener(
            topics = "orders-dlt",
            groupId = "order-service-dlt-group"
    )
    void handleDeadLetterMessage(
            @Payload String event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) int offset,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage,
            @Header(KafkaHeaders.EXCEPTION_STACKTRACE) String stackTrace
    ){
        log.error("=== DEAD LETTER MESSAGE RECEIVED ===");
        log.error("Order ID: {}", event != null ? event : "NULL");
        log.error("Key: , Partition: {}, Offset: {}", partition, offset);
        log.error("Exception: {}", exceptionMessage);
        log.error("Stack Trace: {}", stackTrace);
        log.error("Event: {}", event);
        log.error("====================================");


        // Here you would typically:
        // 1. Send alert to monitoring system
        // 2. Store in database for manual review
        // 3. Notify operations team
        // 4. Create support ticket
    }

}
