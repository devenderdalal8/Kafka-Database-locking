package org.database.bookmyshow.kafka.scheduler;

import org.database.bookmyshow.kafka.producer.OrderOutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderOutboxPublisherScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderOutboxPublisherScheduler.class);
    private final OrderOutboxService outboxService;

    OrderOutboxPublisherScheduler(OrderOutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @Scheduled(fixedRate = 5000)
    void publishPendingEvents(){
        try {
            log.debug("Checking for pending outbox events...");
            outboxService.publishPendingEvents();
        } catch (Exception e) {
            log.error("Error publishing outbox events: {}", e.getMessage(), e);
        }
    }
}
