package com.fooddelivery.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.order.event.PaymentProcessedEvent;
import com.fooddelivery.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProcessedConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.payment-processed:payment-processed}", groupId = "order-payment-consumer-group")
    public void handlePaymentProcessed(Object message) {
        log.info("Received message from Kafka topic 'payment-processed': {}", message);
        try {
            String jsonPayload;
            if (message instanceof org.apache.kafka.clients.consumer.ConsumerRecord<?, ?> record) {
                jsonPayload = String.valueOf(record.value());
            } else if (message instanceof String str) {
                jsonPayload = str;
            } else {
                jsonPayload = objectMapper.writeValueAsString(message);
            }

            log.info("Extracting PaymentProcessed JSON payload: {}", jsonPayload);
            PaymentProcessedEvent event = objectMapper.readValue(jsonPayload, PaymentProcessedEvent.class);

            log.info("Successfully parsed PaymentProcessedEvent: orderId={}, orderNumber={}, status={}",
                    event.getOrderId(), event.getOrderNumber(), event.getPaymentStatus());
            orderService.handlePaymentProcessedEvent(event);
        } catch (Exception e) {
            log.error("Error handling PaymentProcessedEvent: {}", message, e);
        }
    }


}
