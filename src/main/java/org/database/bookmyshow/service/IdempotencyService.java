package org.database.bookmyshow.service;


import org.database.bookmyshow.entity.Idempotency;
import org.database.bookmyshow.entity.Seat;
import org.database.bookmyshow.repository.IdempotencyRepository;
import org.database.bookmyshow.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service responsible for handling seat bookings with idempotency and concurrency control.
 */
@Service
public class IdempotencyService {
    @Autowired
    private IdempotencyRepository idempotencyRepository;
    @Autowired
    private SeatRepository seatRepository;

    /**
     * Books a seat for a specific show.
     * Uses an idempotency key to prevent duplicate processing of the same request.
     * Implements optimistic locking via the Seat entity to handle concurrent booking attempts.
     *
     * @param showId The ID of the show.
     * @param seatNumber The seat identifier.
     * @param idempotencyKey Unique key for the request.
     * @return Success message or previous response if key exists.
     */
    @Transactional
    public String bookSeat(Long showId, String seatNumber , String idempotencyKey) {
        Optional<Idempotency> idempotency = idempotencyRepository
                .findByIdempotencyKey(idempotencyKey);
        if(idempotency.isPresent()){
            return idempotency.get().getResponse();
        }

        Seat seat = seatRepository
                .findBySeatNumberAndShowId(seatNumber, showId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        if (seat.isBooked()) {
            throw new RuntimeException("Seat already booked");
        }

        // Optimistic Locking: The @Version field in the Seat entity (if present)
        // will ensure that if another thread updated this seat, a ObjectOptimisticLockingFailureException is thrown.
        seat.setBooked(true);

        String response = "Seat booked successfully";

        // 🔥 STEP 3: Save idempotency record
        Idempotency record = new Idempotency();
        record.setIdempotencyKey(idempotencyKey);
        record.setResponse(response);
        record.setStatus("SUCCESS");

        idempotencyRepository.save(record);
        seatRepository.save(seat);

        return response;
    }
}
