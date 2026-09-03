package com.fooddelivery.order.kafka;

import com.fooddelivery.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.order-created:order-created}")
    private String orderCreatedTopic;

    @Value("${app.kafka.topics.order-paid:order-paid}")
    private String orderPaidTopic;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        logger.info("Publishing OrderCreatedEvent to Kafka topic '{}': orderId={}, orderNumber={}, restaurantId={}",
                orderCreatedTopic, event.getOrderId(), event.getOrderNumber(), event.getRestaurantId());

        kafkaTemplate.send(orderCreatedTopic, event.getOrderNumber(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Successfully sent OrderCreatedEvent: offset={}", result.getRecordMetadata().offset());
                    } else {
                        logger.error("Failed to send OrderCreatedEvent for orderId={}: {}", event.getOrderId(), ex.getMessage());
                    }
                });
    }

    public void sendOrderPaidEvent(OrderCreatedEvent event) {
        logger.info("Publishing OrderPaidEvent to Kafka topic '{}': orderId={}, orderNumber={}, restaurantId={}",
                orderPaidTopic, event.getOrderId(), event.getOrderNumber(), event.getRestaurantId());

        kafkaTemplate.send(orderPaidTopic, event.getOrderNumber(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Successfully sent OrderPaidEvent: offset={}", result.getRecordMetadata().offset());
                    } else {
                        logger.error("Failed to send OrderPaidEvent for orderId={}: {}", event.getOrderId(), ex.getMessage());
                    }
                });
    }
}

