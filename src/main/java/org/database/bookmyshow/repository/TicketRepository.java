package org.database.bookmyshow.repository;

import jakarta.persistence.LockModeType;
import org.database.bookmyshow.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.seatNumber = :seatNumber AND t.showId = :showId ")
    Optional<Ticket> findTicketBySeatNumberAndShowId(@Param("seatNumber") String seatNumber, @Param("showId") Long showId);
}
