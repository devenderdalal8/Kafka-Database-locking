package org.database.bookmyshow.kafka.entitiy;

public record OrderRequest(
            String customerId,
            String productId,
            Integer quantity,
            java.math.BigDecimal totalAmount
    ) {
    }