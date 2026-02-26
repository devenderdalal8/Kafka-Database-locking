package org.database.bookmyshow.repository;

import org.database.bookmyshow.entity.Idempotency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<Idempotency, Long> {

    Optional<Idempotency> findByIdempotencyKey(String key);
}