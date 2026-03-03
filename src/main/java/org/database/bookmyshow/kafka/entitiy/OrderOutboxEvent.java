package org.database.bookmyshow.kafka.entitiy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "order_outbox_event")
public class OrderOutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String aggregateId;
    @Column(nullable = false)
    private String eventType;
    @Column(nullable = false)
    private String aggregateType;
    @Column(nullable = false , columnDefinition = "TEXT")
    private String payload;

    @Column
    private String eventVersion;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false)
    private LocalDateTime publishedAt = LocalDateTime.now();

    private Integer retryCount = 0;

    private String errorMessage;

    private String partitionKey;

    public OrderOutboxEvent(String aggregateId, String aggregateType, String eventType, String eventVersion, String payload, String partitionKey) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.eventVersion = eventVersion;
        this.payload = payload;
        this.partitionKey = partitionKey;
        this.createdAt = LocalDateTime.now();
        this.eventType = eventType;
        this.aggregateType = aggregateType;
    }
}


