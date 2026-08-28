package com.fooddelivery.restaurant.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.restaurant.event.OrderCreatedEvent;
import com.fooddelivery.restaurant.model.RestaurantOrder;
import com.fooddelivery.restaurant.model.RestaurantOrderStatus;
import com.fooddelivery.restaurant.repository.RestaurantOrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final RestaurantOrderRepository restaurantOrderRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.order-created:order-created}",
            groupId = "${spring.kafka.consumer.group-id:restaurant-service-group}"
    )
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        logger.info("Received OrderCreatedEvent from Kafka for restaurantId={}, orderId={}, orderNumber={}",
                event.getRestaurantId(), event.getOrderId(), event.getOrderNumber());

        String itemsSummary = "";
        try {
            itemsSummary = objectMapper.writeValueAsString(event.getItems());
        } catch (Exception e) {
            logger.warn("Could not serialize items for order: {}", event.getOrderNumber());
        }

        RestaurantOrder restaurantOrder = RestaurantOrder.builder()
                .orderId(event.getOrderId())
                .orderNumber(event.getOrderNumber())
                .restaurantId(event.getRestaurantId())
                .customerId(event.getUserId())
                .totalAmount(event.getTotalAmount())
                .status(RestaurantOrderStatus.PENDING)
                .deliveryAddress(event.getDeliveryAddress())
                .contactPhone(event.getContactPhone())
                .specialInstructions(event.getSpecialInstructions())
                .itemsSummary(itemsSummary)
                .build();

        restaurantOrderRepository.save(restaurantOrder);
        logger.info("Saved incoming restaurant order (ID={}, Status=PENDING)", restaurantOrder.getId());
    }
}
