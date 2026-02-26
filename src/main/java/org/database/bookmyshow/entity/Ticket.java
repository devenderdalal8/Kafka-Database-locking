package org.database.bookmyshow.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "tickets", uniqueConstraints = {@UniqueConstraint(columnNames = {"seat_number", "show_id"})})
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Column(name = "seat_number", nullable = false)
    String seatNumber;

    @Column(name = "show_id", nullable = false)
    Long showId;

    @Column(nullable = false)
    boolean booked = false;

    // 🔥 IMPORTANT FOR OPTIMISTIC LOCKING
    @Version
    private Long version;
}
