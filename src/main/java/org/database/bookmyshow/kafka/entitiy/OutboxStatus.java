package org.database.bookmyshow.kafka.entitiy;

public enum OutboxStatus{
    PENDING,
    PUBLISHED,
    FAILED
}
