package com.fooddelivery.delivery.dto;

import com.fooddelivery.delivery.model.DeliveryStatus;
import com.fooddelivery.delivery.model.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTrackingResponse {
    private Long orderId;
    private String orderNumber;
    private DeliveryStatus deliveryStatus;
    private Long partnerId;
    private String partnerName;
    private String partnerPhone;
    private VehicleType vehicleType;
    private String vehicleNumber;
    private Double partnerLatitude;
    private Double partnerLongitude;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
}
