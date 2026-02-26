package org.database.bookmyshow.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderPlacedEvent(
    String orderId,
    String customerId,
    String productId,
    Integer quantity,
    BigDecimal totalAmount,
    LocalDateTime orderDate
){

}