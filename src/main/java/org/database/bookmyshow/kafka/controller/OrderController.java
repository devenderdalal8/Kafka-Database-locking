package org.database.bookmyshow.kafka.controller;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.database.bookmyshow.kafka.entitiy.OrderRequest;
import org.database.bookmyshow.kafka.entitiy.Orders;
import org.database.bookmyshow.kafka.event.OrderPlacedEvent;
import org.database.bookmyshow.kafka.producer.OrderEventProducer;
import org.database.bookmyshow.kafka.producer.OrderOutboxService;
import org.database.bookmyshow.kafka.reposiotry.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderEventProducer orderEventProducer;
    private final OrderOutboxService outboxService;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    public OrderController(OrderEventProducer orderEventProducer, OrderOutboxService outboxService, ObjectMapper objectMapper, OrderRepository orderRepository) {
        this.orderEventProducer = orderEventProducer;
        this.outboxService = outboxService;
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/place")
    @Transactional  // Important: Both writes in same transaction
    public ResponseEntity<String> placeOrderWithOutBox(@RequestBody OrderRequest request) {
        try {
            log.info("Placing order with outbox event {}", request);
            // Generate order ID

            // Create order entity
            var order = new Orders(request.customerId(), request.productId(), request.quantity(), request.totalAmount(), LocalDateTime.now().toString());

            // Save order to database
            orderRepository.save(order);

            log.info("Placing order with id {}", request.customerId());

            // Create event
            var event = new OrderPlacedEvent(order.getId(), request.customerId(), request.productId(), request.quantity(), request.totalAmount(), LocalDateTime.now());

            // Save to outbox (same transaction!)
            outboxService.saveToOutbox(
                    order.getId(), // aggregateId
                    "Order",                    // aggregateType
                    "OrderPlaced",              // eventType
                    "1.0",                      // eventVersion
                    event,                      // event object
                    order.getId()                     // partitionKey
            );
            log.info("Placed order with id {}", order.getId());
            // Transaction commits here - both order and outbox event are saved atomically

            return ResponseEntity.status(HttpStatus.CREATED).body("Order placed successfully! Order ID: " + order.getId());
        } catch (Exception e) {
            log.error("Error placing order: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
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
