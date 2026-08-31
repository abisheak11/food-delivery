package com.fooddelivery.delivery.dto;

import com.fooddelivery.delivery.model.DeliveryStatus;
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
public class DeliveryTaskResponse {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private String customerPhone;
    private Long restaurantId;
    private String restaurantName;
    private String pickupAddress;
    private String deliveryAddress;
    private Long partnerId;
    private String partnerName;
    private String partnerPhone;
    private DeliveryStatus status;
    private BigDecimal deliveryFee;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
