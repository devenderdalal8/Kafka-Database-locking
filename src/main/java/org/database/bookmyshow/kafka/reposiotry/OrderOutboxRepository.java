package org.database.bookmyshow.kafka.reposiotry;

import org.database.bookmyshow.kafka.entitiy.OrderOutboxEvent;
import org.database.bookmyshow.kafka.entitiy.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderOutboxRepository extends JpaRepository<OrderOutboxEvent, Long> {

    List<OrderOutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);

    @Query("SELECT o FROM  OrderOutboxEvent o WHERE o.status = 'FAILED' AND o.retryCount < :maxRetries ORDER BY o.createdAt ASC")
    List<OrderOutboxEvent> findFailedEventForRetry(int maxRetries);

    @Modifying
    @Query("UPDATE OrderOutboxEvent o SET o.status = 'PUBLISHED' , o.publishedAt = :publishedAt WHERE o.id = :id")
    void markedAsPublished(Long id, LocalDateTime publishedAt);

    @Modifying
    @Query("UPDATE OrderOutboxEvent o SET o.status = 'Failed' , o.errorMessage = :errorMessage WHERE o.id = :id")
    void markedAsFailed(Long id, String errorMessage);
}
