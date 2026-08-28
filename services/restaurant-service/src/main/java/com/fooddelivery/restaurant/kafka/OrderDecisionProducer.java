package com.fooddelivery.restaurant.kafka;

import com.fooddelivery.restaurant.event.RestaurantOrderDecisionEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderDecisionProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderDecisionProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.restaurant-order-decision:restaurant-order-decision}")
    private String decisionTopic;

    public void sendOrderDecision(RestaurantOrderDecisionEvent event) {
        logger.info("Publishing RestaurantOrderDecisionEvent to Kafka topic '{}': orderId={}, decision={}, restaurantId={}",
                decisionTopic, event.getOrderId(), event.getDecision(), event.getRestaurantId());

        kafkaTemplate.send(decisionTopic, event.getOrderNumber(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Successfully sent RestaurantOrderDecisionEvent: offset={}", result.getRecordMetadata().offset());
                    } else {
                        logger.error("Failed to send RestaurantOrderDecisionEvent for orderId={}: {}", event.getOrderId(), ex.getMessage());
                    }
                });
    }
}
