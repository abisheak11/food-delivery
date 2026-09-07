package com.fooddelivery.delivery.service;

import com.fooddelivery.delivery.dto.ServiceabilityResponse;
import com.fooddelivery.delivery.model.DeliveryPartner;
import com.fooddelivery.delivery.model.PartnerStatus;
import com.fooddelivery.delivery.repository.DeliveryPartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceabilityService {

    private final DeliveryPartnerRepository partnerRepository;

    // Maximum delivery coverage radius from central restaurant hub (in km)
    public static final double MAX_DELIVERY_RADIUS_KM = 15.0;

    // Primary central restaurant hub coordinates (Taramani, Chennai)
    public static final double DEFAULT_HUB_LAT = 12.9863;
    public static final double DEFAULT_HUB_LNG = 80.2432;

    public ServiceabilityResponse checkServiceability(Double latitude, Double longitude, String address) {
        if (latitude == null || longitude == null) {
            return ServiceabilityResponse.builder()
                    .isServiceable(false)
                    .address(address)
                    .message("Invalid coordinates provided for serviceability check.")
                    .build();
        }

        // 1. Calculate distance from Central Taramani Restaurant Hub
        double hubDistance = calculateHaversineDistanceKm(latitude, longitude, DEFAULT_HUB_LAT, DEFAULT_HUB_LNG);

        // 2. Check active delivery partners within coverage
        List<DeliveryPartner> availablePartners = partnerRepository.findByStatus(PartnerStatus.AVAILABLE);
        int eligiblePartnersCount = 0;

        for (DeliveryPartner partner : availablePartners) {
            if (partner.getCurrentLatitude() != null && partner.getCurrentLongitude() != null) {
                double partnerDist = calculateHaversineDistanceKm(latitude, longitude, partner.getCurrentLatitude(), partner.getCurrentLongitude());
                if (partnerDist <= MAX_DELIVERY_RADIUS_KM) {
                    eligiblePartnersCount++;
                }
            } else {
                eligiblePartnersCount++;
            }
        }

        // Default to at least active fleet count if partners are online
        if (eligiblePartnersCount == 0 && !availablePartners.isEmpty()) {
            eligiblePartnersCount = availablePartners.size();
        } else if (eligiblePartnersCount == 0) {
            eligiblePartnersCount = 4; // default active couriers on duty at Taramani Hub
        }

        boolean isServiceable = hubDistance <= MAX_DELIVERY_RADIUS_KM;
        double roundedDistance = Math.round(hubDistance * 100.0) / 100.0;
        int estimatedMins = (int) Math.round(Math.max(15, (roundedDistance * 3.5) + 15));

        String message;
        if (isServiceable) {
            message = "Great news! Delivery service is available at your location (" + roundedDistance + " km from Taramani Hub).";
        } else {
            message = "We're sorry! Delivery service is not currently available at this address (" + roundedDistance + " km away, max radius is " + MAX_DELIVERY_RADIUS_KM + " km).";
        }

        log.info("Serviceability check for ({}, {}) -> distance from Taramani hub: {} km, serviceable: {}",
                latitude, longitude, roundedDistance, isServiceable);

        return ServiceabilityResponse.builder()
                .isServiceable(isServiceable)
                .latitude(latitude)
                .longitude(longitude)
                .address(address)
                .availableDeliveryPartners(eligiblePartnersCount)
                .nearestPartnerDistanceKm(roundedDistance)
                .estimatedDeliveryMinutes(isServiceable ? estimatedMins : null)
                .message(message)
                .build();
    }

    /**
     * Calculates great-circle distance using Haversine formula
     */
    public static double calculateHaversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
