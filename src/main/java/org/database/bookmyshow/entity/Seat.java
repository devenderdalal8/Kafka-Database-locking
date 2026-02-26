package org.database.bookmyshow.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"seat_number", "show_id"})
        }
)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Column(nullable = false)
    private boolean booked = false;

    // 🔥 IMPORTANT FOR OPTIMISTIC LOCKING
    @Version
    private Long version;

    public Seat() {}

    public Seat(String seatNumber, Long showId) {
        this.seatNumber = seatNumber;
        this.showId = showId;
    }

    // Getters & Setters

    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public void setShowId(Long showId) { this.showId = showId; }

    public void setBooked(boolean booked) { this.booked = booked; }

}
