package org.database.bookmyshow.kafka.entitiy;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "orders")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    String id;
    @Column(name = "customer_id", nullable = false)
    String customerId;
    @Column(name = "product_id", nullable = false)
    String productId;
    @Column(name = "quantity", nullable = false)
    Integer quantity;
    @Column(name = "total_amount", nullable = false)
    BigDecimal totalAmount;
    @Column(name = "order_date", nullable = false)
    String orderDate;
}
