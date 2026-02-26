package org.database.bookmyshow.repository;

import org.database.bookmyshow.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    Optional<Seat> findBySeatNumberAndShowId(String seatNumber, Long showId);
}
