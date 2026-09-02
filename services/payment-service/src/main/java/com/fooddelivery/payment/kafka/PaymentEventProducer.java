package com.fooddelivery.payment.kafka;

import com.fooddelivery.payment.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.payment-processed:payment-processed}")
    private String paymentProcessedTopic;

    public void publishPaymentProcessedEvent(PaymentProcessedEvent event) {
        log.info("Publishing PaymentProcessedEvent to topic [{}] for Order ID: {}, Status: {}",
                paymentProcessedTopic, event.getOrderId(), event.getPaymentStatus());
        kafkaTemplate.send(paymentProcessedTopic, String.valueOf(event.getOrderId()), event);
    }
}
