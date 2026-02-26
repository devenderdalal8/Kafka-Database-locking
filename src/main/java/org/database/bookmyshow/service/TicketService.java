package org.database.bookmyshow.service;

import org.database.bookmyshow.entity.Ticket;
import org.database.bookmyshow.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Ticket save(String seatNumber, Long showId) {
        Ticket ticket = ticketRepository.findTicketBySeatNumberAndShowId(seatNumber, showId).orElseThrow(() -> new RuntimeException("Ticket not found"));

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (ticket.isBooked()) {
            throw new RuntimeException("Ticket already booked");
        }
        ticket.setBooked(true);
        return ticket;
    }

}
