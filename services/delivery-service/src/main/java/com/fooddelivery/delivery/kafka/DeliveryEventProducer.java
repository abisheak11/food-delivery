package com.fooddelivery.delivery.kafka;

import com.fooddelivery.delivery.event.DeliveryStatusEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.delivery-status:delivery-status-updated}")
    private String deliveryStatusTopic;

    public void sendDeliveryStatusEvent(DeliveryStatusEvent event) {
        logger.info("Publishing delivery status event for order #{}: {}", event.getOrderNumber(), event.getStatus());
        kafkaTemplate.send(deliveryStatusTopic, event.getOrderNumber(), event);
    }
}
