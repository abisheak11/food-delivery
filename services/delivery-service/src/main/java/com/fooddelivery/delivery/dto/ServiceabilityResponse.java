package com.fooddelivery.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceabilityResponse {
    private Boolean isServiceable;
    private Double latitude;
    private Double longitude;
    private String address;
    private Integer availableDeliveryPartners;
    private Double nearestPartnerDistanceKm;
    private Integer estimatedDeliveryMinutes;
    private String message;
}
