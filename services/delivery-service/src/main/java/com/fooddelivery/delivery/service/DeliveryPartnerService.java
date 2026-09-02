package com.fooddelivery.delivery.service;

import com.fooddelivery.delivery.dto.DeliveryPartnerProfileRequest;
import com.fooddelivery.delivery.dto.DeliveryPartnerResponse;
import com.fooddelivery.delivery.dto.UpdateAvailabilityRequest;
import com.fooddelivery.delivery.dto.UpdateLocationRequest;
import com.fooddelivery.delivery.exception.ResourceNotFoundException;
import com.fooddelivery.delivery.model.DeliveryPartner;
import com.fooddelivery.delivery.model.PartnerStatus;
import com.fooddelivery.delivery.repository.DeliveryPartnerRepository;
import com.fooddelivery.delivery.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository partnerRepository;

    @Transactional
    public DeliveryPartnerResponse createOrUpdateProfile(DeliveryPartnerProfileRequest request, UserPrincipal currentUser) {
        DeliveryPartner partner = partnerRepository.findByUserId(currentUser.getId())
                .orElse(null);

        if (partner == null) {
            partner = DeliveryPartner.builder()
                    .userId(currentUser.getId())
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .vehicleType(request.getVehicleType())
                    .vehicleNumber(request.getVehicleNumber())
                    .licenseNumber(request.getLicenseNumber())
                    .status(PartnerStatus.AVAILABLE)
                    .totalDeliveries(0)
                    .rating(5.0)
                    .build();
        } else {
            partner.setFullName(request.getFullName());
            partner.setPhone(request.getPhone());
            partner.setVehicleType(request.getVehicleType());
            partner.setVehicleNumber(request.getVehicleNumber());
            partner.setLicenseNumber(request.getLicenseNumber());
        }

        DeliveryPartner savedPartner = partnerRepository.save(partner);
        return mapToResponse(savedPartner);
    }

    public DeliveryPartnerResponse getCurrentPartnerProfile(UserPrincipal currentUser) {
        DeliveryPartner partner = partnerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner profile not found for current user"));
        return mapToResponse(partner);
    }

    @Transactional
    public DeliveryPartnerResponse updateAvailability(UpdateAvailabilityRequest request, UserPrincipal currentUser) {
        DeliveryPartner partner = partnerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner profile not found"));

        partner.setStatus(request.getStatus());
        DeliveryPartner updated = partnerRepository.save(partner);
        return mapToResponse(updated);
    }

    @Transactional
    public DeliveryPartnerResponse updateLocation(UpdateLocationRequest request, UserPrincipal currentUser) {
        DeliveryPartner partner = partnerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner profile not found"));

        partner.setCurrentLatitude(request.getLatitude());
        partner.setCurrentLongitude(request.getLongitude());
        DeliveryPartner updated = partnerRepository.save(partner);
        return mapToResponse(updated);
    }

    public List<DeliveryPartnerResponse> getAllPartners(PartnerStatus status) {
        List<DeliveryPartner> partners;
        if (status != null) {
            partners = partnerRepository.findByStatus(status);
        } else {
            partners = partnerRepository.findAll();
        }
        return partners.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public DeliveryPartner getPartnerByUserId(Long userId) {
        return partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner profile not found for user ID: " + userId));
    }

    public DeliveryPartnerResponse mapToResponse(DeliveryPartner partner) {
        return DeliveryPartnerResponse.builder()
                .id(partner.getId())
                .userId(partner.getUserId())
                .fullName(partner.getFullName())
                .phone(partner.getPhone())
                .vehicleType(partner.getVehicleType())
                .vehicleNumber(partner.getVehicleNumber())
                .licenseNumber(partner.getLicenseNumber())
                .status(partner.getStatus())
                .currentLatitude(partner.getCurrentLatitude())
                .currentLongitude(partner.getCurrentLongitude())
                .totalDeliveries(partner.getTotalDeliveries())
                .rating(partner.getRating())
                .createdAt(partner.getCreatedAt())
                .build();
    }
}
