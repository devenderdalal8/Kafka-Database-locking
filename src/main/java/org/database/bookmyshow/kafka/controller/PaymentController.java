package org.database.bookmyshow.kafka.controller;

import org.database.bookmyshow.kafka.producer.PaymentEventProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.database.bookmyshow.kafka.entitiy.PaymentRequest;


@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    private final PaymentEventProducer paymentEventProducer;
    
    public PaymentController(PaymentEventProducer paymentEventProducer) {
        this.paymentEventProducer = paymentEventProducer;
    }
    
    @PostMapping("/process")
    public ResponseEntity<String> processPayment(
        @RequestBody PaymentRequest request
    ) {
        try {
            paymentEventProducer.processPaymentTransactionally(
                request.orderId(),
                request.customerId(),
                request.amount()
            );
            
            return ResponseEntity.ok(
                "Payment processed successfully for order: " + request.orderId()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Payment processing failed: " + e.getMessage());
        }
    }

}