package com.fooddelivery.order.kafka;

import com.fooddelivery.order.event.RestaurantOrderDecisionEvent;
import com.fooddelivery.order.model.Order;
import com.fooddelivery.order.model.OrderStatus;
import com.fooddelivery.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
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

    @KafkaListener(
            topics = "${app.kafka.topics.restaurant-order-decision:restaurant-order-decision}",
            groupId = "${spring.kafka.consumer.group-id:order-service-group}"
    )
    @Transactional
    public void handleRestaurantDecision(RestaurantOrderDecisionEvent event) {
        logger.info("Received RestaurantOrderDecisionEvent from Kafka: orderId={}, orderNumber={}, decision={}, reason={}",
                event.getOrderId(), event.getOrderNumber(), event.getDecision(), event.getReason());

        Order order = orderRepository.findById(event.getOrderId())
                .or(() -> orderRepository.findByOrderNumber(event.getOrderNumber()))
                .orElse(null);

        if (order == null) {
            logger.warn("Order not found for decision event: orderId={}, orderNumber={}", event.getOrderId(), event.getOrderNumber());
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
    }
}
