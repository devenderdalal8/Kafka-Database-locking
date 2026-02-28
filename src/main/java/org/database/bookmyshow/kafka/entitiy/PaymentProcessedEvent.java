package org.database.bookmyshow.kafka.entitiy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentProcessedEvent(
        String paymentId,
        String orderId,
        String customerId,
        BigDecimal amount,
        String status,
        LocalDateTime processedAt
) {
}