package org.database.bookmyshow.kafka.controller;

import org.database.bookmyshow.kafka.entitiy.OrderRequest;
import org.database.bookmyshow.kafka.event.OrderPlacedEvent;
import org.database.bookmyshow.kafka.producer.OrderEventProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderEventProducer orderEventProducer;

    public OrderController(OrderEventProducer orderEventProducer) {
        this.orderEventProducer = orderEventProducer;
    }

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest orderRequest) {
        String orderId = UUID.randomUUID().toString();

        OrderPlacedEvent event = new OrderPlacedEvent(
                orderId,
                orderRequest.customerId(),
                orderRequest.productId(),
                orderRequest.quantity(),
                orderRequest.totalAmount(),
                LocalDateTime.now()
        );

        orderEventProducer.sendOrderPlaceEvent(event);

        return ResponseEntity.ok("Orders placed successfully with ID: " + orderId);
    }

}
