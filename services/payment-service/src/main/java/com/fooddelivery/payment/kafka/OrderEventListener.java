package com.fooddelivery.payment.kafka;

import com.fooddelivery.payment.event.OrderCreatedEvent;
import com.fooddelivery.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final PaymentService paymentService;

    @KafkaListener(topics = "${app.kafka.topics.order-created:order-created}", groupId = "${spring.kafka.consumer.group-id:payment-service-group}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for Order ID: {}, Order Number: {}, Amount: {}",
                event.getOrderId(), event.getOrderNumber(), event.getTotalAmount());
        try {
            paymentService.handleOrderCreatedEvent(event);
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent for Order ID: {}", event.getOrderId(), e);
        }
    }
}
