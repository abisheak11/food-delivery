package com.fooddelivery.restaurant.dto;

import com.fooddelivery.restaurant.model.RestaurantOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantOrderResponse {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long restaurantId;
    private Long customerId;
    private BigDecimal totalAmount;
    private RestaurantOrderStatus status;
    private String deliveryAddress;
    private String contactPhone;
    private String specialInstructions;
    private String itemsSummary;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
