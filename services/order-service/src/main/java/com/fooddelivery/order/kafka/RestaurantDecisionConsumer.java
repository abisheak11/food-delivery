package com.fooddelivery.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.order.event.RestaurantOrderDecisionEvent;
import com.fooddelivery.order.model.Order;
import com.fooddelivery.order.model.OrderStatus;
import com.fooddelivery.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestaurantDecisionConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantDecisionConsumer.class);

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.restaurant-order-decision:restaurant-order-decision}", groupId = "order-restaurant-consumer-group")
    @Transactional
    public void handleRestaurantDecision(Object message) {
        logger.info("Received message from Kafka topic 'restaurant-order-decision': {}", message);
        try {
            String payload;
            if (message instanceof org.apache.kafka.clients.consumer.ConsumerRecord<?, ?> record) {
                payload = String.valueOf(record.value());
            } else if (message instanceof String str) {
                payload = str;
            } else {
                payload = objectMapper.writeValueAsString(message);
            }

            logger.info("Raw restaurant-order-decision payload: {}", payload);
            RestaurantOrderDecisionEvent event = objectMapper.readValue(payload, RestaurantOrderDecisionEvent.class);


            logger.info(
                    "Received RestaurantOrderDecisionEvent from Kafka: orderId={}, orderNumber={}, decision={}, reason={}",
                    event.getOrderId(), event.getOrderNumber(), event.getDecision(), event.getReason());

            Order order = null;
            if (event.getOrderId() != null) {
                order = orderRepository.findById(event.getOrderId()).orElse(null);
            }
            if (order == null && event.getOrderNumber() != null) {
                order = orderRepository.findByOrderNumber(event.getOrderNumber()).orElse(null);
            }

            if (order == null) {
                logger.warn("Order not found for decision event: orderId={}, orderNumber={}", event.getOrderId(),
                        event.getOrderNumber());
                return;
            }

            if ("ACCEPTED".equalsIgnoreCase(event.getDecision())) {
                order.setStatus(OrderStatus.CONFIRMED);
                logger.info("Order status updated to CONFIRMED for orderId={}", order.getId());
            } else if ("REJECTED".equalsIgnoreCase(event.getDecision())) {
                order.setStatus(OrderStatus.CANCELLED);
                logger.info("Order status updated to CANCELLED (Rejected by restaurant) for orderId={}", order.getId());
            }

            orderRepository.save(order);
        } catch (Exception e) {
            logger.error("Error processing RestaurantOrderDecisionEvent from message {}: {}", message, e.getMessage(), e);
        }
    }
}


