package org.database.bookmyshow;

import lombok.RequiredArgsConstructor;
import org.database.bookmyshow.entity.Ticket;
import org.database.bookmyshow.service.IdempotencyService;
import org.database.bookmyshow.service.SeatService;
import org.database.bookmyshow.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seats")

public class BookingController {

    @Autowired
    private SeatService seatService;

    @Autowired
    private TicketService ticketService;
    @Autowired
    private IdempotencyService idempotencyService;

    public BookingController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping("/book")
    public String bookSeat(@RequestParam Long showId, @RequestParam String seatNumber) {

        return seatService.bookSeat(showId, seatNumber);
    }

    @PostMapping("/ticket")
    public Ticket bookTicket(@RequestParam Long showId, @RequestParam String seatNumber) {
        return ticketService.save(seatNumber, showId);
    }

    @PostMapping("/bookTicket")
    public ResponseEntity<String> bookSeat(
            @RequestParam Long showId,
            @RequestParam String seatNumber,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        String response =
                idempotencyService.bookSeat(showId, seatNumber, idempotencyKey);

        return ResponseEntity.ok(response);
    }
}