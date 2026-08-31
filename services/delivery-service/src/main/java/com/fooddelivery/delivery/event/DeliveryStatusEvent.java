package com.fooddelivery.delivery.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStatusEvent implements Serializable {
    private Long deliveryId;
    private Long orderId;
    private String orderNumber;
    private Long partnerId;
    private String partnerName;
    private String partnerPhone;
    private String status; // "ASSIGNED", "PICKED_UP", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"
    private LocalDateTime timestamp;
    private String notes;
}
