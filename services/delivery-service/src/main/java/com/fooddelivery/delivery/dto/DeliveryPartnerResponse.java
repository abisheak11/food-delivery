package com.fooddelivery.delivery.dto;

import com.fooddelivery.delivery.model.PartnerStatus;
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
public class DeliveryPartnerResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String phone;
    private VehicleType vehicleType;
    private String vehicleNumber;
    private String licenseNumber;
    private PartnerStatus status;
    private Double currentLatitude;
    private Double currentLongitude;
    private Integer totalDeliveries;
    private Double rating;
    private LocalDateTime createdAt;
}
