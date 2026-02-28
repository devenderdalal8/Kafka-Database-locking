package org.database.bookmyshow.kafka.entitiy;

import java.math.BigDecimal;

public record PaymentRequest(String orderId, String customerId, BigDecimal amount) {
}