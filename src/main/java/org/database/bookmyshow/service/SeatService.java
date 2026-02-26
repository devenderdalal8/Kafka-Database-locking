package org.database.bookmyshow.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.database.bookmyshow.entity.Seat;
import org.database.bookmyshow.exception.SeatAlreadyBookedException;
import org.database.bookmyshow.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Transactional
    public String bookSeat(Long showId, String seatNumber) {

        Seat seat = seatRepository
                .findBySeatNumberAndShowId(seatNumber, showId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        if (seat.isBooked()) {
            throw new SeatAlreadyBookedException("Seat already booked");
        }

        seat.setBooked(true);

        // No explicit save needed (JPA dirty checking)
        // On commit, Hibernate will:
        // UPDATE seats SET booked=true, version=version+1
        // WHERE id=? AND version=?

        return "Seat booked successfully";
    }
}
