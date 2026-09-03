package com.fooddelivery.delivery.kafka;

import com.fooddelivery.delivery.event.OrderCreatedEvent;
import com.fooddelivery.delivery.model.DeliveryStatus;
import com.fooddelivery.delivery.model.DeliveryTask;
import com.fooddelivery.delivery.repository.DeliveryTaskRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final DeliveryTaskRepository deliveryTaskRepository;

    @KafkaListener(topics = "${app.kafka.topics.order-paid:order-paid}", groupId = "${spring.kafka.consumer.group-id:delivery-service-group}")
    public void handleOrderCreated(OrderCreatedEvent event) {

        logger.info("Received OrderCreatedEvent for order #{}: total={}", event.getOrderNumber(), event.getTotalAmount());

        try {
            if (deliveryTaskRepository.existsByOrderId(event.getOrderId())) {
                logger.info("Delivery task already exists for order #{}", event.getOrderNumber());
                return;
            }

            DeliveryTask task = DeliveryTask.builder()
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .customerId(event.getUserId())
                    .customerPhone(event.getContactPhone())
                    .restaurantId(event.getRestaurantId())
                    .restaurantName("Restaurant #" + event.getRestaurantId())
                    .pickupAddress("Restaurant #" + event.getRestaurantId() + " Address")
                    .deliveryAddress(event.getDeliveryAddress())
                    .deliveryFee(BigDecimal.valueOf(4.99))
                    .notes(event.getSpecialInstructions())
                    .status(DeliveryStatus.PENDING)
                    .build();

            deliveryTaskRepository.save(task);
            logger.info("Created pending delivery task for order #{}", event.getOrderNumber());
        } catch (Exception e) {
            logger.error("Error processing OrderCreatedEvent for order #{}: {}", event.getOrderNumber(), e.getMessage(), e);
        }
    }
}
