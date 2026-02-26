package org.database.bookmyshow.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@Data
@NoArgsConstructor
@Table(name = "idempotency_key", uniqueConstraints = {@UniqueConstraint(columnNames = {"idempotency_key"})})
public class Idempotency {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Lob
    private String response;

    private String status; // SUCCESS / FAILED

}
