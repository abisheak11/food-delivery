package com.fooddelivery.delivery.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent implements Serializable {
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private Long restaurantId;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String contactPhone;
    private String specialInstructions;
    private List<OrderItemEventDto> items;
    private LocalDateTime createdAt;
}
